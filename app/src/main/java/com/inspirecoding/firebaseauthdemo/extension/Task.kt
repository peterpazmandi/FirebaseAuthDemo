package com.inspirecoding.firebaseauthdemo.extension

import com.google.android.gms.tasks.Task
import com.inspirecoding.firebaseauthdemo.utils.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Task Extension
suspend fun <T> Task<T>.await(): Result<T>
{
    if (isComplete)
    {
        val e = exception
        return if (e == null)
        {
            if (isCanceled)
            {
                Result.Canceled(CancellationException("Task $this was cancelled normally."))
            }
            else
            {
                @Suppress("UNCHECKED_CAST")
                Result.Success(result as T)
            }
        }
        else
        {
            Result.Error(e)
        }
    }

    return suspendCancellableCoroutine { cont ->
        addOnCompleteListener {
            val e = exception
            if (e == null)
            {
                @Suppress("UNCHECKED_CAST")
                if (isCanceled)
                {
                    cont.cancel()
                }
                else
                {
                    cont.resume(Result.Success(result as T))
                }
            }
            else
            {
                cont.resumeWithException(e)
            }
        }
    }
}