package com.example.android.githubsearchwithnotifications.utils;

import android.text.TextUtils;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class GitHubUtils {
    private final static String GITHUB_SEARCH_BASE_URL = "https://api.github.com/search/repositories";
    private final static String GITHUB_SEARCH_QUERY_PARAM = "q";
    private final static String GITHUB_SEARCH_SORT_PARAM = "sort";
    private final static String GITHUB_SEARCH_LANGUAGE_FORMAT_STR = "language:%s";
    private final static String GITHUB_SEARCH_USER_FORMAT_STR = "user:%s";
    private final static String GITHUB_SEARCH_IN_FORMAT_STR = "in:%s";
    private final static String GITHUB_SEARCH_IN_NAME = "name";
    private final static String GITHUB_SEARCH_IN_DESCRIPTION = "description";
    private final static String GITHUB_SEARCH_IN_README = "readme";

    public static String buildGitHubSearchQueryTerm(String query, String language, String user,
                                                    boolean inName, boolean inDescription,
                                                    boolean inReadme) {
        String queryTerm = new String(query);

        if (!TextUtils.isEmpty(language)) {
            queryTerm += " " + String.format(GITHUB_SEARCH_LANGUAGE_FORMAT_STR, language);
        }

        if (!TextUtils.isEmpty(user)) {
            queryTerm += " " + String.format(GITHUB_SEARCH_USER_FORMAT_STR, user);
        }

        String searchIn = buildSearchInTerm(inName, inDescription, inReadme);
        if (!TextUtils.isEmpty(searchIn)) {
            queryTerm += " " + String.format(GITHUB_SEARCH_IN_FORMAT_STR, searchIn);
        }

        return queryTerm;
    }

    @Nullable
    private static String buildSearchInTerm(boolean inName, boolean inDescription,
                                            boolean inReadme) {
        ArrayList<String> searchInTerms = new ArrayList<>();
        if (inName) {
            searchInTerms.add(GITHUB_SEARCH_IN_NAME);
        }
        if (inDescription) {
            searchInTerms.add(GITHUB_SEARCH_IN_DESCRIPTION);
        }
        if (inReadme) {
            searchInTerms.add(GITHUB_SEARCH_IN_README);
        }

        if (!searchInTerms.isEmpty()) {
            return TextUtils.join(",", searchInTerms);
        } else {
            return null;
        }
    }
}
