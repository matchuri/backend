package matchuri.backend.global.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class RollbackRecordExecutorTest {

    private final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
    private final RollbackRecordExecutor executor = new RollbackRecordExecutor(transactionManager);
    private final Runnable record = mock(Runnable.class);

    @BeforeEach
    void beginBusinessTransaction() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
    }

    @AfterEach
    void cleanTransactionState() {
        TransactionSynchronizationManager.clear();
    }

    @Test
    void rollbackRecordsInNewTransactionAfterCompletion() {
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        executor.afterRollback("allowed-failure", record);
        verifyNoInteractions(record, transactionManager);
        complete(TransactionSynchronization.STATUS_ROLLED_BACK);

        verify(record).run();
        ArgumentCaptor<TransactionDefinition> definition = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(transactionManager).getTransaction(definition.capture());
        assertThat(definition.getValue().getPropagationBehavior()).isEqualTo(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        verify(transactionManager).commit(any());
    }

    @Test
    void successfulTransactionDoesNotWriteFailureRecords() {
        executor.afterRollback("allowed-failure", record);
        complete(TransactionSynchronization.STATUS_COMMITTED);
        verifyNoInteractions(record, transactionManager);
    }

    @Test
    void unknownCompletionDoesNotAssumeRollback() {
        executor.afterRollback("allowed-failure", record);
        complete(TransactionSynchronization.STATUS_UNKNOWN);
        verifyNoInteractions(record, transactionManager);
    }

    @Test
    void recordFailureDoesNotReplaceOriginalBusinessError() {
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        executor.afterRollback("allowed-failure", () -> { throw new IllegalStateException("record storage unavailable"); });
        assertThatCode(() -> complete(TransactionSynchronization.STATUS_ROLLED_BACK)).doesNotThrowAnyException();
        verify(transactionManager).rollback(any());
    }

    @Test
    void missingBusinessTransactionIsRejected() {
        TransactionSynchronizationManager.clear();
        assertThatThrownBy(() -> executor.afterRollback("allowed-failure", record)).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(record, transactionManager);
    }

    private void complete(int status) {
        TransactionSynchronizationManager.getSynchronizations().forEach(synchronization -> synchronization.afterCompletion(status));
    }
}
