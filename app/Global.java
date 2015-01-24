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


    /**
     * The name of the persistence unit we will be using.
     */
    static final String DEFAULT_PERSISTENCE_UNIT = "defaultPersistenceUnit";

    /**
     * Declare the application context to be used - a Java annotation based application context requiring no XML.
     */
    final private AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();

    /**
     * Sync the context lifecycle with Play's.
     */
    @Override
    public void onStart(final Application app) {
        super.onStart(app);
        startJPAConfigs();
    }

    /**
     * Controllers must be resolved through the application context. There is a special method of GlobalSettings
     * that we can override to resolve a given controller. This resolution is required by the Play router.
     */
    @Override
    public <A> A getControllerInstance(Class<A> aClass) {
        return ctx.getBean(aClass);
    }

    /**
     * Sync the context lifecycle with Play's.
     */
    @Override
    public void onStop(final Application app) {
        // This will call any destruction lifecycle methods and then release the beans e.g. @PreDestroy
        ctx.close();
        super.onStop(app);
    }

    /**
     * Starts the JPA configuration and scan packages
     */
    private void startJPAConfigs() {
        ctx.register(SpringDataJpaConfiguration.class);
        ctx.scan("models", "controllers", "repositories", "services");
        ctx.refresh();
        // Construct the beans and call any construction lifecycle methods e.g. @PostConstruct
        ctx.start();
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
