package eu.cloudteams.controller;

import eu.cloudteams.util.github.GithubService;
import net.minidev.json.JSONArray;
import org.eclipse.egit.github.core.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static eu.cloudteams.controller.GithubStatisticsTO.getValue;

/**
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public final class GithubStatisticsTO {

    private final GithubService githubService;
    private final Repository repository;
    private List<User> collaboratorsList;
    private List<Label> labelsList;
    private List<RepositoryBranch> branchesList;
    private List<RepositoryCommit> commits;
    private CommitsStats commitsStats;
    private JSONObject labelsCount;
    private Map<String, Long> labelsCountMap;

    public Repository getRepository() {
        return repository;
    }

    public List<User> getCollaborators() {
        return collaboratorsList;
    }

    public List<RepositoryBranch> getBranches() {
        return branchesList;
    }

    public List<RepositoryCommit> getCommits() {
        return this.commits;
    }

    public List<Label> getLabels() {
        return this.labelsList;
    }

    public JSONObject getLabelsGroup() {
        return this.labelsCount;
    }
    /**
     * @return the labelsCountMap
     */
    public Map<String, Long> getLabelsCountMap() {
        return labelsCountMap;
    }

    public GithubStatisticsTO(GithubService githubService, Repository repository) throws IOException {
        this.githubService = githubService;
        this.repository = repository;

        repository.setDescription(getValue(repository.getDescription()));

        gatherInfo();
    }

    public void gatherInfo() throws IOException {

        labelsCountMap = githubService.getIssueService()
                .getIssues(repository, null)
                .stream().map(Issue::getLabels)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(label -> label.getName(), Collectors.counting()));

        labelsCount = new JSONObject(getLabelsCountMap());
        /*
        JSONObject jsonLabels = new JSONObject();
        JSONArray jsonLabelsCount = new JSONArray();

        Iterator iterator = null;
        iterator = labelsCountMap.entrySet().iterator();
        while (iterator.hasNext()) {
            JSONObject jsonTemp = new JSONObject();
            Map.Entry pair = (Map.Entry) iterator.next();

            jsonTemp.put("name", pair.getKey());
            jsonTemp.put("y", pair.getValue());

            jsonLabelsCount.add(jsonTemp);

            iterator.remove();
        }

        jsonLabels.put("labelCount", jsonLabelsCount);


        System.out.println("labelscount---->" + labelsCount.toString());
        System.out.println("labelscountForGraphs---->" + jsonLabels.toString());
        */
        branchesList = githubService.getGithubRepositoryService().getBranches(repository);
        labelsList = githubService.getLabelService().getLabels(repository);
        commits = githubService.getCommitService().getCommits(repository);

        //Code section (info for master branch)
        labelsList = githubService.getLabelService().getLabels(repository);
        collaboratorsList = githubService.getCollaboratorService().getCollaborators(repository);

        //Pulse section
        setLatMonthCommitsStats();
    }


    public GithubStatisticsTO(GithubService githubService, Repository repository, String not_used) throws IOException {
        this.githubService = githubService;
        this.repository = repository;

        // repository.setDescription(getValue(repository.getDescription()));

        // gatherInfoForcharts();
    }


    public JSONObject gatherInfoForcharts() throws IOException {
        JSONObject jsonLabels = new JSONObject();

        Map<String, Long> labelsCountMap = githubService.getIssueService()
                .getIssues(repository, null)
                .stream().map(Issue::getLabels)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(label -> label.getName(), Collectors.counting()));

        labelsCount = new JSONObject(labelsCountMap);

        JSONArray jsonLabelsCount = new JSONArray();

        Iterator iterator = null;
        iterator = labelsCountMap.entrySet().iterator();
        while (iterator.hasNext()) {
            JSONObject jsonTemp = new JSONObject();
            Map.Entry pair = (Map.Entry) iterator.next();

            jsonTemp.put("name", pair.getKey());
            jsonTemp.put("y", pair.getValue());

            jsonLabelsCount.add(jsonTemp);

            iterator.remove();
        }

        jsonLabels.put("labelCount", jsonLabelsCount);


        System.out.println("labelscount---->" + labelsCount.toString());
        System.out.println("labelscountForGraphs---->" + jsonLabels.toString());

        return jsonLabels;
    }

    private void setLatMonthCommitsStats() {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -7);
        //final List<RepositoryCommit> lastMonthcommits = commits.stream().filter(commit -> commit.getCommit().getCommitter().getDate().getTime() > cal.getTime().getTime())  .collect(Collectors.toList());

        List<String> commitsSHA = commits.stream().filter(commit -> commit.getCommit().getCommitter().getDate().getTime() > cal.getTime().getTime()).map(commit -> commit.getSha()).collect(Collectors.toList());

        Set<String> contributors = new HashSet<>();

        int totalAdditions = 0;
        int totalDeletions = 0;

        //Get latest commits
        for (String commitSHA : commitsSHA) {
            try {

                RepositoryCommit tmpRepositoryCommit = githubService.getCommitService().getCommit(repository, commitSHA);
                contributors.add(tmpRepositoryCommit.getCommitter().getLogin());
                totalAdditions += tmpRepositoryCommit.getStats().getAdditions();
                totalDeletions += tmpRepositoryCommit.getStats().getDeletions();
            } catch (IOException ex) {
                Logger.getLogger(GithubStatisticsTO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

//Set commitsStats
        this.commitsStats = new CommitsStats(totalAdditions, totalDeletions, commitsSHA.size(), contributors);
    }

    public CommitsStats getCommitsStats() {
        return this.commitsStats;
    }

    public static String getValue(String value) {
        return (null == value || value.isEmpty()) ? "N/A" : value;
    }


}

class CommitsStats {

    private final int totalAdditions;
    private final int totalDeletions;
    private final int totalCommits;
    private final Set<String> contributors;

    public CommitsStats(int totalAdditions, int totalDeletions, int totalCommits, Set<String> contributors) {
        this.totalAdditions = totalAdditions;
        this.totalDeletions = totalDeletions;
        this.totalCommits = totalCommits;
        this.contributors = contributors;

    }

    public int getTotalAdditions() {
        return this.totalAdditions;
    }

    public int getTotalDeletions() {
        return this.totalDeletions;
    }

    public int getTotalCommits() {
        return this.totalCommits;
    }

    public int getTotalChanges() {
        return this.totalAdditions + this.totalDeletions;
    }

    public int getContributors() {
        return this.contributors.size();
    }

    public String getContributorsNames() {
        return getValue(this.contributors.stream().collect(Collectors.joining(" ")));
    }

}
