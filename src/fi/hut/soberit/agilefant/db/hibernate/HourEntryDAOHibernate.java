package fi.hut.soberit.agilefant.db.hibernate;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

import fi.hut.soberit.agilefant.db.HourEntryDAO;
import fi.hut.soberit.agilefant.model.BacklogHourEntry;
import fi.hut.soberit.agilefant.model.HourEntry;
import fi.hut.soberit.agilefant.model.StoryHourEntry;
import fi.hut.soberit.agilefant.model.TaskHourEntry;
import fi.hut.soberit.agilefant.model.User;

@Repository("hourEntryDAO")
public class HourEntryDAOHibernate extends GenericDAOHibernate<HourEntry>
        implements HourEntryDAO {

    public HourEntryDAOHibernate() {
        super(HourEntry.class);
    }

    public long calculateSumByUserAndTimeInterval(User user,
            DateTime startDate, DateTime endDate) {
        Criteria crit = getCurrentSession().createCriteria(HourEntry.class);
        crit.add(Restrictions.eq("user", user));
        crit.add(Restrictions.between("date", startDate, endDate));
        crit.setProjection(Projections.sum("minutesSpent"));
        Long result = (Long) crit.uniqueResult();
        if (result == null)
            return 0;
        return result;
    }

    public long calculateSumByStory(int storyId) {
        Criteria crit = getCurrentSession().createCriteria(TaskHourEntry.class);
        crit.setProjection(Projections.sum("minutesSpent"));
        crit.createCriteria("task").createCriteria("story").add(
                Restrictions.idEq(storyId));
        Long result = (Long) crit.uniqueResult();
        if (result == null)
            return 0;
        return result;
    }

    public long calculateSumFromTasksWithoutStory(int iterationId) {
        Criteria crit = getCurrentSession().createCriteria(TaskHourEntry.class);
        crit.setProjection(Projections.sum("minutesSpent"));
        Criteria taskCrit = crit.createCriteria("task");
        taskCrit.add(Restrictions.isNull("story"));
        taskCrit.createCriteria("iteration")
                .add(Restrictions.idEq(iterationId));
        Long result = (Long) crit.uniqueResult();
        if (result == null)
            return 0;
        return result;
    }
    
    private void setDateUserFilter(Criteria crit, DateTime start, DateTime end, Set<Integer> users) {
        if(start != null) {
            crit.add(Restrictions.ge("date", start));
        }
        if(end != null) {
            crit.add(Restrictions.le("date", end));
        }
        if(users != null && users.size() > 0) {
            crit.createAlias("user", "usr");
            crit.add(Restrictions.in("usr.id", users));
        }
    }

    public List<BacklogHourEntry> getBacklogHourEntriesByFilter(
            Set<Integer> backlogIds, DateTime startDate, DateTime endDate,
            Set<Integer> userIds) {
        if (backlogIds == null || backlogIds.size() == 0) {
            return Collections.emptyList();
        }
        Criteria crit = getCurrentSession().createCriteria(
                BacklogHourEntry.class);
        crit.createAlias("backlog", "bl", CriteriaSpecification.LEFT_JOIN);
        crit.createAlias("backlog.parent", "blParent",
                CriteriaSpecification.LEFT_JOIN);
        crit.createAlias("backlog.parent.parent", "blParentParent",
                CriteriaSpecification.LEFT_JOIN);
        crit.add(Restrictions.or(Restrictions.in("bl.id", backlogIds),
                Restrictions.or(Restrictions.in("blParent.id", backlogIds),
                        Restrictions.in("blParentParent.id", backlogIds))));
        this.setDateUserFilter(crit, startDate, endDate, userIds);
        return asList(crit);
    }

    public List<StoryHourEntry> getStoryHourEntriesByFilter(
            Set<Integer> backlogIds, DateTime startDate, DateTime endDate,
            Set<Integer> userIds) {
        if (backlogIds == null || backlogIds.size() == 0) {
            return Collections.emptyList();
        }

        Criteria crit = getCurrentSession()
                .createCriteria(StoryHourEntry.class);
        crit
                .createAlias("story.backlog", "bl",
                        CriteriaSpecification.LEFT_JOIN);
        crit.createAlias("story.backlog.parent", "blParent",
                CriteriaSpecification.LEFT_JOIN);
        crit.createAlias("story.backlog.parent.parent", "blParentParent",
                CriteriaSpecification.LEFT_JOIN);

        crit.add(Restrictions.or(Restrictions.in("bl.id", backlogIds),
                Restrictions.or(Restrictions.in("blParent.id", backlogIds),
                        Restrictions.in("blParentParent.id", backlogIds))));
        this.setDateUserFilter(crit, startDate, endDate, userIds);
        return asList(crit);
    }

    public List<TaskHourEntry> getTaskHourEntriesByFilter(
            Set<Integer> backlogIds, DateTime startDate, DateTime endDate,
            Set<Integer> userIds) {
        if(backlogIds == null || backlogIds.size() == 0) {
            return Collections.emptyList();
        }
        
        Criteria crit = getCurrentSession().createCriteria(TaskHourEntry.class);
        
        crit.createAlias("task.story.backlog", "bl",
                CriteriaSpecification.LEFT_JOIN);
        crit.createAlias("task.story.backlog.parent", "blParent",
                CriteriaSpecification.LEFT_JOIN);
        crit.createAlias("task.story.backlog.parent.parent", "blParentParent",
                CriteriaSpecification.LEFT_JOIN);
        crit.createAlias("task.iteration", "iBl", CriteriaSpecification.LEFT_JOIN);
        crit.createAlias("task.iteration.parent", "iBlParent",
                CriteriaSpecification.LEFT_JOIN);
        crit.createAlias("task.iteration.parent.parent", "iBlParentParent",
                CriteriaSpecification.LEFT_JOIN);

        Criterion iterationParents = Restrictions.or(Restrictions.in(
                "iBlParent.id", backlogIds), Restrictions.in("iBlParentParent.id",
                backlogIds));
        Criterion iterationParent = Restrictions.or(iterationParents,
                Restrictions.in("iBl.id", backlogIds));
        Criterion parentIteration = Restrictions.or(iterationParent,
                Restrictions.in("bl.id", backlogIds));
        Criterion parentProject = Restrictions.or(parentIteration, Restrictions
                .in("blParent.id", backlogIds));
        crit.add(Restrictions.or(Restrictions.in("blParentParent.id",
                backlogIds), parentProject));
        this.setDateUserFilter(crit, startDate, endDate, userIds);
        return asList(crit);
    }

}
