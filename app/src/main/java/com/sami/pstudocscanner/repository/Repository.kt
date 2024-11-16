package com.sami.pstudocscanner.repository

import android.app.Activity
import android.net.Uri
import com.sami.pstudocscanner.util.checkAndCreateInternalParentDir
import com.sami.pstudocscanner.util.getListFiles
import kotlinx.coroutines.coroutineScope
import javax.inject.Singleton

@Singleton
class Repository(private val context: Activity) {

    private val _docList = mutableListOf<Pair<Uri, String>>()

    suspend fun fetchData(): List<Pair<Uri, String>> {
        coroutineScope {
            checkAndCreateInternalParentDir(context).let { directory ->
                _docList.addAll(getListFiles(directory))
            }
        }
        return _docList
    }
}
