package com.query.pattern;

import static org.assertj.core.api.Assertions.assertThat;

import com.query.pattern.entity.Member;
import com.query.pattern.entity.QMember;
import com.query.pattern.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PatternApplicationTests {

  @PersistenceContext
  EntityManager em;

  @BeforeEach
  public void before() {
    Team teamA = Team.builder()
        .name("teamA")
        .build();
    Team teamB = Team.builder()
        .name("teamB")
        .build();
    em.persist(teamA);
    em.persist(teamB);
    Member member1 = Member.builder()
        .username("member1")
        .age(20)
        .team(teamA)
        .build();
    Member member2 = Member.builder()
        .username("member2")
        .age(30)
        .team(teamA)
        .build();
    Member member3 = Member.builder()
        .username("member3")
        .age(40)
        .team(teamB)
        .build();
    Member member4 = Member.builder()
        .username("member4")
        .age(50)
        .team(teamB)
        .build();
    em.persist(member1);
    em.persist(member2);
    em.persist(member3);
    em.persist(member4);
    //초기화
    em.flush();
    em.clear();
    //확인
    List<Member> members = em.createQuery("select m from Member m", Member.class)
        .getResultList();
    for (Member member : members) {
      System.out.println("member=" + member);
      System.out.println("-> member.team=" + member.getTeam());
    }
  }

  public void startJPQL() {
    //member1을 찾아라
    String qlString = "select m from Member m where m.username = :username";

    Member jpqlMember = em.createQuery(qlString, Member.class)
        .setParameter("username", "member1")
        .getSingleResult();

    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    QMember m = new QMember("m");
    Member findMember = queryFactory
        .select(m)
        .from(m)
        .where(m.username.eq("member1"))//파라미터 바인딩 처리
        .fetchOne();

    assertThat(jpqlMember.getUsername()).isEqualTo(findMember.getTeam());
  }

  @Test
  public void search() {
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    QMember member = new QMember("m");

    Member findMember = queryFactory
        .selectFrom(member)
        .where(member.username.eq("member1")
            .and(member.age.eq(10)))
        .fetchOne();

    /**
     * member.username.isNotNull() //이름이 is not null
     * member.age.in(10, 20) // age in (10,20)
     * member.age.notIn(10, 20) // age not in (10, 20)
     * member.age.between(10,30) //between 10, 30
     * member.age.goe(30) // age >= 30
     * member.age.gt(30) // age > 30
     * member.age.loe(30) // age <= 30
     * member.age.lt(30) // age < 30
     * member.username.like("member%") //like 검색
     * member.username.contains("member") // like ‘%member%’ 검색
     * member.username.startsWith("member") //like ‘member%’ 검색
     */

    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  public void searchAndParam() {
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    QMember member = new QMember("m");

    List<Member> result1 = queryFactory
        .selectFrom(member)
        .where(member.username.eq("member1"),
            member.age.eq(10))
        .fetch();
    assertThat(result1.size()).isEqualTo(1);
  }
}
