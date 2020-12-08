package au.id.jaysee.helpers;

import au.id.jaysee.minecraft.config.Configuration;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.OptionalIterable;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.input.*;
import org.apache.commons.collections4.IteratorUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class JiraIssuesHelper {
    private JiraRestClient jiraRestClient;
    private Configuration configuration;

    public JiraIssuesHelper(JiraRestClient jiraRestClient, Configuration configuration) {
        this.jiraRestClient = jiraRestClient;
        this.configuration = configuration;
    }

    public String createIssue(Player player, String summary, Location location) {
        Iterator<Field> fields = jiraRestClient
                .getMetadataClient()
                .getFields().claim().iterator();

        List<Field> fieldsList = IteratorUtils.toList(fields);

        Field world = getCustomFieldIdByName(fieldsList, "World");
        Field x = getCustomFieldIdByName(fieldsList, "X");
        Field y = getCustomFieldIdByName(fieldsList, "Y");
        Field z = getCustomFieldIdByName(fieldsList, "Z");

        IssueInputBuilder input = new IssueInputBuilder();
        input.setProjectKey(configuration.getProjectKey());

        input.setFieldValue(x.getId(), String.valueOf(location.getX()));
        input.setFieldValue(y.getId(), String.valueOf(location.getY()));
        input.setFieldValue(z.getId(), String.valueOf(location.getZ()));
        input.setFieldValue(world.getId(), location.getWorld().getName());

        List<IssueType> issueTypeList = IteratorUtils.toList(jiraRestClient
                .getProjectClient()
                .getProject(configuration.getProjectKey())
                .claim()
                .getIssueTypes().iterator());

        IssueType issueType = issueTypeList.stream().filter(issueType1 -> !issueType1.isSubtask()).findAny().orElse(null);

        if(configuration.getParentKey() != null && !configuration.getParentKey().isBlank()) {
            Map<String, Object> parent = new HashMap<String, Object>();
            parent.put("key", configuration.getParentKey());
            input.setFieldInput(new FieldInput("parent", new ComplexIssueInputFieldValue(parent)));
            issueType = issueTypeList.stream().filter(IssueType::isSubtask).findAny().orElse(null);
        }

        input.setIssueType(issueType);

        input.setSummary(summary);
        return jiraRestClient.getIssueClient()
                .createIssue(input.build()).claim().getKey();
    }

    public Issue getIssueByKey(String key) {
        return jiraRestClient.getIssueClient()
                .getIssue(key).claim();
    }

    public List<Field> getFields() {
        return IteratorUtils.toList(jiraRestClient
                .getMetadataClient()
                .getFields().claim().iterator());
    }

    public Field getCustomFieldIdByName(List<Field> fields, String name) {
        return fields.stream().filter(f -> f.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public List<Issue> getIssues() {
        String jqlString = "project = " + configuration.getProjectKey() + " and status !='Done'";

        if(configuration.getParentKey() != null && !configuration.getParentKey().isBlank()) {
            jqlString += " and parent=" + configuration.getParentKey();
        }

        Iterator<Issue> issueIterator = jiraRestClient
                .getSearchClient()
                .searchJql(jqlString)
                .claim()
                .getIssues().iterator();

        return IteratorUtils.toList(issueIterator);
    }

    public Boolean resolveIssue(String transition, String key) {
        try {
            Integer transitionNo = transition.equals("TODO") ? configuration.getJiraProgressTransition() : configuration.getJiraDoneTransition();
            jiraRestClient
                    .getIssueClient()
                    .transition(
                            jiraRestClient.getIssueClient().getIssue(key).claim(),
                            new TransitionInput(transitionNo)
                    ).claim();
            return true;
        } catch (RestClientException e) {
            return false;
        }
    }
}