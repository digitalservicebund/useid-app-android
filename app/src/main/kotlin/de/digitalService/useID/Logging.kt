package de.digitalService.useID

import android.util.Log
import org.slf4j.Logger
import org.slf4j.Marker

fun <T : Any> T.getLogger(): Lazy<Logger> = lazy {
    // Disable slf4j logger until we resolve the dependency clash

//     LoggerFactory.getLogger(this::class.java)

    object : Logger {
        private val tag = "USEID-LOG"

        override fun getName(): String {
            TODO("Not yet implemented")
        }

        override fun isTraceEnabled(): Boolean {
            TODO("Not yet implemented")
        }

        override fun isTraceEnabled(marker: Marker?): Boolean {
            TODO("Not yet implemented")
        }

        override fun trace(msg: String?) {
            msg?.let { Log.v(tag, it) }
        }

        override fun trace(format: String?, arg: Any?) {
            TODO("Not yet implemented")
        }

        override fun trace(format: String?, arg1: Any?, arg2: Any?) {
            TODO("Not yet implemented")
        }

        override fun trace(format: String?, vararg arguments: Any?) {
            TODO("Not yet implemented")
        }

        override fun trace(msg: String?, t: Throwable?) {
            TODO("Not yet implemented")
        }

        override fun trace(marker: Marker?, msg: String?) {
            TODO("Not yet implemented")
        }

        override fun trace(marker: Marker?, format: String?, arg: Any?) {
            TODO("Not yet implemented")
        }

        override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
            TODO("Not yet implemented")
        }

        override fun trace(marker: Marker?, format: String?, vararg argArray: Any?) {
            TODO("Not yet implemented")
        }

        override fun trace(marker: Marker?, msg: String?, t: Throwable?) {
            TODO("Not yet implemented")
        }

        override fun isDebugEnabled(): Boolean {
            TODO("Not yet implemented")
        }

        override fun isDebugEnabled(marker: Marker?): Boolean {
            TODO("Not yet implemented")
        }

        override fun debug(msg: String?) {
            msg?.let { Log.d(tag, it) }
        }

        override fun debug(format: String?, arg: Any?) {
            TODO("Not yet implemented")
        }

        override fun debug(format: String?, arg1: Any?, arg2: Any?) {
            TODO("Not yet implemented")
        }

        override fun debug(format: String?, vararg arguments: Any?) {
            TODO("Not yet implemented")
        }

        override fun debug(msg: String?, t: Throwable?) {
            TODO("Not yet implemented")
        }

        override fun debug(marker: Marker?, msg: String?) {
            TODO("Not yet implemented")
        }

        override fun debug(marker: Marker?, format: String?, arg: Any?) {
            TODO("Not yet implemented")
        }

        override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
            TODO("Not yet implemented")
        }

        override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {
            TODO("Not yet implemented")
        }

        override fun debug(marker: Marker?, msg: String?, t: Throwable?) {
            TODO("Not yet implemented")
        }

        override fun isInfoEnabled(): Boolean {
            TODO("Not yet implemented")
        }

        override fun isInfoEnabled(marker: Marker?): Boolean {
            TODO("Not yet implemented")
        }

        override fun info(msg: String?) {
            msg?.let { Log.i(tag, it) }
        }

        override fun info(format: String?, arg: Any?) {
            TODO("Not yet implemented")
        }

        override fun info(format: String?, arg1: Any?, arg2: Any?) {
            TODO("Not yet implemented")
        }

        override fun info(format: String?, vararg arguments: Any?) {
            TODO("Not yet implemented")
        }

        override fun info(msg: String?, t: Throwable?) {
            TODO("Not yet implemented")
        }

        override fun info(marker: Marker?, msg: String?) {
            TODO("Not yet implemented")
        }

        override fun info(marker: Marker?, format: String?, arg: Any?) {
            TODO("Not yet implemented")
        }

        override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
            TODO("Not yet implemented")
        }

        override fun info(marker: Marker?, format: String?, vararg arguments: Any?) {
            TODO("Not yet implemented")
        }

        override fun info(marker: Marker?, msg: String?, t: Throwable?) {
            TODO("Not yet implemented")
        }

        override fun isWarnEnabled(): Boolean {
            TODO("Not yet implemented")
        }

        override fun isWarnEnabled(marker: Marker?): Boolean {
            TODO("Not yet implemented")
        }

        override fun warn(msg: String?) {
            msg?.let { Log.w(tag, it) }
        }

        override fun warn(format: String?, arg: Any?) {
            TODO("Not yet implemented")
        }

        override fun warn(format: String?, vararg arguments: Any?) {
            TODO("Not yet implemented")
        }

        override fun warn(format: String?, arg1: Any?, arg2: Any?) {
            TODO("Not yet implemented")
        }

        override fun warn(msg: String?, t: Throwable?) {
            TODO("Not yet implemented")
        }

        override fun warn(marker: Marker?, msg: String?) {
            TODO("Not yet implemented")
        }

        override fun warn(marker: Marker?, format: String?, arg: Any?) {
            TODO("Not yet implemented")
        }

        override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
            TODO("Not yet implemented")
        }

        override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) {
            TODO("Not yet implemented")
        }

        override fun warn(marker: Marker?, msg: String?, t: Throwable?) {
            TODO("Not yet implemented")
        }

        override fun isErrorEnabled(): Boolean {
            TODO("Not yet implemented")
        }

        override fun isErrorEnabled(marker: Marker?): Boolean {
            TODO("Not yet implemented")
        }

        override fun error(msg: String?) {
            msg?.let { Log.e(tag, it) }
        }

        override fun error(format: String?, arg: Any?) {
            TODO("Not yet implemented")
        }

        override fun error(format: String?, arg1: Any?, arg2: Any?) {
            TODO("Not yet implemented")
        }

        override fun error(format: String?, vararg arguments: Any?) {
            TODO("Not yet implemented")
        }

        override fun error(msg: String?, t: Throwable?) {
            TODO("Not yet implemented")
        }

        override fun error(marker: Marker?, msg: String?) {
            TODO("Not yet implemented")
        }

        override fun error(marker: Marker?, format: String?, arg: Any?) {
            TODO("Not yet implemented")
        }

        override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
            TODO("Not yet implemented")
        }

        override fun error(marker: Marker?, format: String?, vararg arguments: Any?) {
            TODO("Not yet implemented")
        }

        override fun error(marker: Marker?, msg: String?, t: Throwable?) {
            TODO("Not yet implemented")
        }
    }
}
