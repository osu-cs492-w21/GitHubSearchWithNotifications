package com.example.android.githubsearchwithnotifications;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.android.githubsearchwithnotifications.data.GitHubRepo;

import java.util.List;

public class BookmarkedRepos extends AppCompatActivity implements GitHubSearchAdapter.OnSearchResultClickListener {

    private BookmarkedReposViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarked_repos);

        RecyclerView bookmarkedReposRV = findViewById(R.id.rv_bookmarked_repos);
        bookmarkedReposRV.setLayoutManager(new LinearLayoutManager(this));
        bookmarkedReposRV.setHasFixedSize(true);

        GitHubSearchAdapter adapter = new GitHubSearchAdapter(this);
        bookmarkedReposRV.setAdapter(adapter);

        this.viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication())
        ).get(BookmarkedReposViewModel.class);

        this.viewModel.getAllBookmarkedRepos().observe(
                this,
                new Observer<List<GitHubRepo>>() {
                    @Override
                    public void onChanged(List<GitHubRepo> gitHubRepos) {
                        adapter.updateSearchResults(gitHubRepos);
                    }
                }
        );
    }

    @Override
    public void onSearchResultClicked(GitHubRepo repo) {
        Intent intent = new Intent(this, RepoDetailActivity.class);
        intent.putExtra(RepoDetailActivity.EXTRA_GITHUB_REPO, repo);
        startActivity(intent);
    }
}