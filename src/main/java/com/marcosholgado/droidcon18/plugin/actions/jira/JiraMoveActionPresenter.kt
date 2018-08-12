package com.marcosholgado.droidcon18.plugin.actions.jira

import android.util.Log
import com.intellij.openapi.project.Project
import com.intellij.util.Base64
import com.marcosholgado.droidcon18.plugin.actions.jira.network.JiraService
import com.marcosholgado.droidcon18.plugin.components.JiraComponent
import git4idea.repo.GitRepositoryManager
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class JiraMoveActionPresenter @Inject constructor(val view: JiraMoveAction, val project: Project, val jiraService: JiraService) {

    var disposable: Disposable? = null

    fun getBranch() {
        val repositoryManager = GitRepositoryManager.getInstance(project!!)
        var branch = ""

        for (repository in repositoryManager.repositories) {
            branch = repository.currentBranch!!.name
            break
        }

        view.setBranch(branch)
        getTransitions(branch)
    }

    private fun getTransitions(branch: String) {

        val auth = getAuthCode()
        disposable = jiraService.getTransitions(auth,branch)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(
                        { response ->
                            for(transition in response.transitions) {
                                view.addTransition(transition)
                            }
                        },
                        { error ->
                            Log.d("ERROR", error.message)
                        }
                )
    }

    private fun getAuthCode() : String {
        val component = JiraComponent.getInstance(project!!)
        val username = component.username
        val password = component.password

        val data: ByteArray = "$username:$password".toByteArray(Charsets.UTF_8)
        return "Basic " + Base64.encode(data)
    }
}