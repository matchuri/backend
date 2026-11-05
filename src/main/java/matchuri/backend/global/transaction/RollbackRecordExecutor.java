package matchuri.backend.global.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Saves explicitly allowed failure records only after the business transaction has rolled back. */
@Slf4j
@Component
public class RollbackRecordExecutor {

    private final TransactionTemplate recordTransaction;

    public RollbackRecordExecutor(PlatformTransactionManager transactionManager) {
        recordTransaction = new TransactionTemplate(transactionManager);
        recordTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void afterRollback(String recordType, Runnable record) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Failure records require an active business transaction");
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_ROLLED_BACK) {
                    return;
                }
                try {
                    recordTransaction.executeWithoutResult(transactionStatus -> record.run());
                } catch (RuntimeException exception) {
                    // Keep the original API failure; retry/compensation remains a separate decision.
                    log.error("Rollback record persistence failed: type={}", recordType, exception);
                }
            }
        });
    }
}
