package com.veeyikpong.fastnews.ui.search

import com.veeyikpong.fastnews.ui.base.BasePresenter
import com.veeyikpong.fastnews.BuildConfig
import com.veeyikpong.fastnews.api.ApiService
import com.veeyikpong.fastnews.api.response.News
import com.veeyikpong.fastnews.api.response.SearchNewsResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject

class SearchPresenter(val view: SearchFragment) : BasePresenter(), SearchContract.Presenter, KoinComponent {

    private val apiService: ApiService by inject()

    var newsList: List<News>

    var getPostDisposable: Disposable? = null

    init {
        newsList = ArrayList()
    }

    override fun init() {
        if (isInitialized) {
            view.showNews(newsList)
            return
        }

        //Perform some first time operations here
        //For example, get data, which only needs to call one time when the fragment is created, but not when pop

        isInitialized = true
    }

    override fun searchNews(query: String) {
        view.showLoading()
        getPostDisposable = apiService.searchNews(query, BuildConfig.API_KEY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeWith(object: DisposableObserver<SearchNewsResponse>() {
                override fun onComplete() {

                }

                override fun onNext(response: SearchNewsResponse?) {
                    view.hideLoading()
                    if(response!!.status.toLowerCase() != "ok"){
                        view.showError()
                        return
                    }

                    view.displayTotalResult(response.totalResults.toString())
                    newsList = response.newsList
                    view.showNews(response.newsList)
                }

                override fun onError(e: Throwable?) {
                    view.hideLoading()
                    view.showError()
                }
            })
    }

    override fun onDestroy() {
        if(getPostDisposable!=null && !getPostDisposable!!.isDisposed){
            getPostDisposable!!.dispose()
        }
    }
}
