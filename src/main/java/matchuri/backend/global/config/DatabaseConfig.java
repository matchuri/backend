package matchuri.backend.global.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Value("${matchuri.query-monitor.enabled}")
    public boolean queryMonitorEnabled;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariConfig hikariConfig(DataSourceProperties dataSourceProperties) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dataSourceProperties.determineUrl());
        hikariConfig.setUsername(dataSourceProperties.determineUsername());
        hikariConfig.setPassword(dataSourceProperties.determinePassword());
        hikariConfig.setDriverClassName(dataSourceProperties.determineDriverClassName());
        return hikariConfig;
    }

    @Bean(name = "dataSource")
    public DataSource dataSource(HikariConfig hikariConfig) {

        DataSource originalDataSource = new HikariDataSource(hikariConfig);
        if (!queryMonitorEnabled) return originalDataSource;

        Formatter formatter = FormatStyle.BASIC.getFormatter();

        return ProxyDataSourceBuilder.create(originalDataSource)
                .countQuery()
                .logQueryBySlf4j(SLF4JLogLevel.DEBUG)
                .name("matchuri-query-log")
                .formatQuery(formatter::format)
                .multiline()
                .logSlowQueryBySlf4j(2, TimeUnit.MINUTES, SLF4JLogLevel.WARN)
                .buildProxy();
    }
}
