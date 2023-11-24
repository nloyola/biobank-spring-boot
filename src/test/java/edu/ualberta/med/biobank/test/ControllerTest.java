package edu.ualberta.med.biobank.test;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import edu.ualberta.med.biobank.domain.CSMUser;
import edu.ualberta.med.biobank.domain.Membership;
import edu.ualberta.med.biobank.domain.Status;
import edu.ualberta.med.biobank.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;

@Testcontainers
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ControllerTest extends BaseTest {
    private static final String GLOBAL_ADMIN_LOGIN = "globaladmin";

    @PersistenceContext
    private EntityManager em;

    protected Factory factory;

    @BeforeEach
    public void setUp() throws Exception {
        this.factory = new Factory(em);
    }

    protected User getOrCreateSuperUser() {
        // check if user already exists
        CriteriaBuilder builder = em.getCriteriaBuilder();
        var cq = builder.createQuery(User.class);
        var scheme = cq.from(User.class);
        var predicate = builder.equal(scheme.get("login"), GLOBAL_ADMIN_LOGIN);
        cq.where(predicate);
        var query = em.createQuery(cq);
        var users = query.getResultList();

        if (users.size() >= 1) return users.get(0);

        CSMUser csmUser = new CSMUser();
        csmUser.setLoginName(factory.getFaker().name().lastName());
        csmUser.setMigratedFlag(true);
        csmUser.setFirstName(factory.getFaker().name().firstName());
        csmUser.setLastName(factory.getFaker().name().lastName());
        csmUser.setUpdateDate(new Date());
        em.persist(csmUser);

        User globalAdmin = new User();
        globalAdmin.setLogin(GLOBAL_ADMIN_LOGIN);
        globalAdmin.setCsmUser(csmUser);
        globalAdmin.setRecvBulkEmails(false);
        globalAdmin.setFullName(GLOBAL_ADMIN_LOGIN);
        globalAdmin.setEmail(GLOBAL_ADMIN_LOGIN);
        globalAdmin.setNeedPwdChange(false);
        globalAdmin.setNeedPwdChange(false);
        globalAdmin.setActivityStatus(Status.ACTIVE);

        em.persist(globalAdmin);

        Membership membership = new Membership();
        membership.getDomain().setAllCenters(true);
        membership.getDomain().setAllStudies(true);

        membership.setUserManager(true);
        membership.setEveryPermission(true);
        membership.setPrincipal(globalAdmin);
        globalAdmin.getMemberships().add(membership);

        em.persist(membership);

        return globalAdmin;
    }
}