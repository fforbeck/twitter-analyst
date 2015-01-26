import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate3.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import play.Application;
import play.GlobalSettings;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Application wide behaviour. Setup spring app context for the dependency injection system,
 * configure Spring Data, JPA and starts the Twitter Harvest Actor system.
 *
 * Based on: https://github.com/typesafehub/play-spring-data-jpa/blob/master/app/Global.java
 */
@Configuration
public class Global extends GlobalSettings {

    public static AnnotationConfigApplicationContext applicationContext;

    /**
     * The name of the persistence unit we will be using.
     */
    static final String DEFAULT_PERSISTENCE_UNIT = "defaultPersistenceUnit";

    /**
     * Sync the context lifecycle with Play's.
     */
    @Override
    public void onStart(Application app) {
        super.onStart(app);
        applicationContext = new AnnotationConfigApplicationContext();
        startJPAConfigs();
        applicationContext.refresh();
        // Construct the beans and call any construction lifecycle methods e.g. @PostConstruct
        applicationContext.start();
    }

    /**
     * Controllers must be resolved through the application context. There is a special method of GlobalSettings
     * that we can override to resolve a given controller. This resolution is required by the Play router.
     */
    @Override
    public <A> A getControllerInstance(Class<A> aClass) {
        return applicationContext.getBean(aClass);
    }

    /**
     * Sync the context lifecycle with Play's.
     */
    @Override
    public void onStop(final Application app) {
        // This will call any destruction lifecycle methods and then release the beans e.g. @PreDestroy
        applicationContext.close();
        super.onStop(app);
    }

    /**
     * Starts the JPA configuration and scan packages
     */
    private void startJPAConfigs() {
        applicationContext.register(SpringDataJpaConfiguration.class);
        applicationContext.scan("models", "controllers", "repositories", "services");
    }

    /**
     * This configuration establishes Spring Data concerns including those of JPA.
     */
    @Configuration
    @EnableJpaRepositories("repositories")
    public static class SpringDataJpaConfiguration {

        @Bean
        public EntityManagerFactory entityManagerFactory() {
            return Persistence.createEntityManagerFactory(DEFAULT_PERSISTENCE_UNIT);
        }

        @Bean
        public HibernateExceptionTranslator hibernateExceptionTranslator() {
            return new HibernateExceptionTranslator();
        }

        @Bean
        public JpaTransactionManager transactionManager() {
            return new JpaTransactionManager();
        }
    }

}
