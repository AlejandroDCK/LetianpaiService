package com.renhejia.robot.letianpaiservice

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import com.letianpai.robot.letianpaiservice.LtpAppCmdCallback
import com.letianpai.robot.letianpaiservice.LtpAudioEffectCallback
import com.letianpai.robot.letianpaiservice.LtpBleCallback
import com.letianpai.robot.letianpaiservice.LtpBleResponseCallback
import com.letianpai.robot.letianpaiservice.LtpExpressionCallback
import com.letianpai.robot.letianpaiservice.LtpIdentifyCmdCallback
import com.letianpai.robot.letianpaiservice.LtpLongConnectCallback
import com.letianpai.robot.letianpaiservice.LtpMcuCommandCallback
import com.letianpai.robot.letianpaiservice.LtpMiCmdCallback
import com.letianpai.robot.letianpaiservice.LtpRobotStatusCallback
import com.letianpai.robot.letianpaiservice.LtpSensorResponseCallback
import com.letianpai.robot.letianpaiservice.LtpSpeechCallback
import com.letianpai.robot.letianpaiservice.LtpTTSCallback
import com.renhejia.robot.letianpaiservice.consts.RobotAidlConsts
import com.renhejia.robot.letianpaiservice.parser.CmdInfo
import java.lang.ref.WeakReference
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * @author liujunbin
 */
class LetianpaiService : Service() {
    private val robotStatus = 0
    private val ROBOT_OTA_STATUS = 1
    private val RemoteCallbackList = RemoteCallbackList<LtpCommandCallback?>()
    private val ltpLongConnectCallback = RemoteCallbackList<LtpLongConnectCallback?>()
    private val ltpMcuCommandCallback = RemoteCallbackList<LtpMcuCommandCallback?>()

    //
    private val ltpAudioEffectCallback = RemoteCallbackList<LtpAudioEffectCallback?>()
    private val ltpExpressionCallback = RemoteCallbackList<LtpExpressionCallback?>()
    private val ltpAppCmdCallback = RemoteCallbackList<LtpAppCmdCallback?>()
    private val ltpRobotStatusCallback = RemoteCallbackList<LtpRobotStatusCallback?>()
    private val ltpTTSCallback = RemoteCallbackList<LtpTTSCallback?>()
    private val ltpSpeechCallback = RemoteCallbackList<LtpSpeechCallback?>()
    private val ltpSensorResponseCallback = RemoteCallbackList<LtpSensorResponseCallback?>()
    private val ltpMiCmdCallback = RemoteCallbackList<LtpMiCmdCallback?>()
    private val ltpIdentifyCmdCallback = RemoteCallbackList<LtpIdentifyCmdCallback?>()
    private val ltpBleCallback = RemoteCallbackList<LtpBleCallback?>()
    private val ltpBleResponseCallback = RemoteCallbackList<LtpBleResponseCallback?>()

    private var robotLongConnectHandler: RobotLongConnectHandler? = null
    private var robotMcuHandler: RobotMcuHandler? = null
    private var robotAudioEffectHandler: RobotAudioEffectHandler? = null
    private var robotExpressionHandler: RobotExpressionHandler? = null
    private var robotAppCmdHandler: RobotAppCmdHandler? = null
    private var robotRobotStatusHandler: RobotRobotStatusHandler? = null
    private var robotTTSHandler: RobotTTSHandler? = null
    private var robotSpeechCmdHandler: RobotSpeechCmdHandler? = null
    private var robotSensorHandler: RobotSensorHandler? = null
    private var robotMiHandler: RobotMiHandler? = null
    private var robotIdentifyHandler: RobotIdentifyHandler? = null
    private var robotBleHandler: RobotBleHandler? = null
    private var robotBleResponseHandler: RobotBleResponseHandler? = null

    override fun onBind(intent: Intent): IBinder? {
        return iLetianpaiService
    }

    private fun initHandler() {
        robotLongConnectHandler = RobotLongConnectHandler(this@LetianpaiService)
        robotMcuHandler = RobotMcuHandler(this@LetianpaiService)
        robotAudioEffectHandler = RobotAudioEffectHandler(this@LetianpaiService)
        robotExpressionHandler = RobotExpressionHandler(this@LetianpaiService)
        robotAppCmdHandler = RobotAppCmdHandler(this@LetianpaiService)
        robotRobotStatusHandler = RobotRobotStatusHandler(this@LetianpaiService)
        robotTTSHandler = RobotTTSHandler(this@LetianpaiService)
        robotSpeechCmdHandler = RobotSpeechCmdHandler(this@LetianpaiService)
        robotSensorHandler = RobotSensorHandler(this@LetianpaiService)
        robotMiHandler = RobotMiHandler(this@LetianpaiService)
        robotIdentifyHandler = RobotIdentifyHandler(this@LetianpaiService)
        robotBleHandler = RobotBleHandler(this@LetianpaiService)
        robotBleResponseHandler = RobotBleResponseHandler(this@LetianpaiService)
    }

    private val mLock: Lock = ReentrantLock()
    private val mMcuLock: Lock = ReentrantLock()
    private val mAeLock: Lock = ReentrantLock()
    private val mAppLock: Lock = ReentrantLock()
    private val mExpressionLock: Lock = ReentrantLock()
    private val mLongConnectLock: Lock = ReentrantLock()
    private val mStatusLock: Lock = ReentrantLock()
    private val mTTSLock: Lock = ReentrantLock()
    private val mSpeechSLock: Lock = ReentrantLock()
    private val mMiLock: Lock = ReentrantLock()
    private val mIdentifyLock: Lock = ReentrantLock()
    private val mSensorLock: Lock = ReentrantLock()
    private val mBleLock: Lock = ReentrantLock()
    private val mBleResponseLock: Lock = ReentrantLock()

    private val iLetianpaiService: ILetianpaiService.Stub = object : ILetianpaiService.Stub() {
        @Throws(RemoteException::class)
        override fun getRobotStatus(): Int {
            return robotStatus
        }

        @Throws(RemoteException::class)
        override fun setCommand(ltpCommand: LtpCommand) {
            if (ltpCommand == null) {
                Log.i("letianpai_server", "ltpCommand is null")
            } else {
                Log.i("letianpai_server", "ltpCommand is not null")
            }
            if (robotStatus == ROBOT_OTA_STATUS) {
            } else {
                responseCommand(ltpCommand)
            }
        }

        @Throws(RemoteException::class)
        override fun setRobotStatus(status: Int) {
            robotStatus = status
            setLTPRobotStatus(robotStatus)
            // TODO 分发命令回调
        }

        @Throws(RemoteException::class)
        override fun registerCallback(cc: LtpCommandCallback) {
            RemoteCallbackList.register(cc)
        }

        @Throws(RemoteException::class)
        override fun unregisterCallback(cc: LtpCommandCallback) {
            RemoteCallbackList.unregister(cc)
        }

        @Throws(RemoteException::class)
        override fun setLongConnectCommand(command: String, data: String) {
//            responseLongConnectCommand(command, data);
            sendLongConnectCmd(command, data)
        }

        @Throws(RemoteException::class)
        override fun registerLCCallback(lcCallback: LtpLongConnectCallback) {
            ltpLongConnectCallback.register(lcCallback)
        }

        @Throws(RemoteException::class)
        override fun unregisterLCCallback(lcCallback: LtpLongConnectCallback) {
            ltpLongConnectCallback.unregister(lcCallback)
        }


        @Throws(RemoteException::class)
        override fun setMcuCommand(command: String, data: String) {
            sendMcuCmd(command, data)
        }

        @Throws(RemoteException::class)
        override fun registerMcuCmdCallback(mcuCallback: LtpMcuCommandCallback) {
            ltpMcuCommandCallback.register(mcuCallback)
        }

        @Throws(RemoteException::class)
        override fun unregisterMcuCmdCallback(mcuCallback: LtpMcuCommandCallback) {
            ltpMcuCommandCallback.unregister(mcuCallback)
        }

        @Throws(RemoteException::class)
        override fun setAudioEffect(command: String, data: String) {
            sendAudioEffectCmd(command, data)
        }

        @Throws(RemoteException::class)
        override fun registerAudioEffectCallback(aeCallback: LtpAudioEffectCallback) {
            ltpAudioEffectCallback.register(aeCallback)
        }

        @Throws(RemoteException::class)
        override fun unregisterAudioEffectCallback(aeCallback: LtpAudioEffectCallback) {
            ltpAudioEffectCallback.unregister(aeCallback)
        }

        @Throws(RemoteException::class)
        override fun setExpression(command: String, data: String) {
            sendExpressionCmd(command, data)
        }

        @Throws(RemoteException::class)
        override fun registerExpressionCallback(expressionCallback: LtpExpressionCallback) {
            ltpExpressionCallback.register(expressionCallback)
        }

        @Throws(RemoteException::class)
        override fun unregisterExpressionCallback(expressionCallback: LtpExpressionCallback) {
            ltpExpressionCallback.unregister(expressionCallback)
        }

        @Throws(RemoteException::class)
        override fun setAppCmd(command: String, data: String) {
            sendAppCmd(command, data)
        }

        @Throws(RemoteException::class)
        override fun registerAppCmdCallback(appCallback: LtpAppCmdCallback) {
            ltpAppCmdCallback.register(appCallback)
        }

        @Throws(RemoteException::class)
        override fun unregisterAppCmdCallback(appCallback: LtpAppCmdCallback) {
            ltpAppCmdCallback.unregister(appCallback)
        }

        @Throws(RemoteException::class)
        override fun setRobotStatusCmd(command: String, data: String) {
            sendRobotStatus(command, data)
        }

        @Throws(RemoteException::class)
        override fun registerRobotStatusCallback(statusCallback: LtpRobotStatusCallback) {
            ltpRobotStatusCallback.register(statusCallback)
        }

        @Throws(RemoteException::class)
        override fun unregisterRobotStatusCallback(statusCallback: LtpRobotStatusCallback) {
            ltpRobotStatusCallback.unregister(statusCallback)
        }

        @Throws(RemoteException::class)
        override fun setTTS(command: String, data: String) {
            sendTTS(command, data)
        }

        @Throws(RemoteException::class)
        override fun registerTTSCallback(ttsCallback: LtpTTSCallback) {
            ltpTTSCallback.register(ttsCallback)
        }

        @Throws(RemoteException::class)
        override fun unregisterTTSCallback(ttsCallback: LtpTTSCallback) {
            ltpTTSCallback.unregister(ttsCallback)
        }


        @Throws(RemoteException::class)
        override fun setSpeechCmd(command: String, data: String) {
            sendSpeechCmd(command, data)
        }

        @Throws(RemoteException::class)
        override fun registerSpeechCallback(speechCallback: LtpSpeechCallback) {
            ltpSpeechCallback.register(speechCallback)
        }

        @Throws(RemoteException::class)
        override fun unregisterSpeechCallback(speechCallback: LtpSpeechCallback) {
            ltpSpeechCallback.unregister(speechCallback)
        }

        @Throws(RemoteException::class)
        override fun setSensorResponse(command: String, data: String) {
            sendSensorCmd(command, data)
        }

        @Throws(RemoteException::class)
        override fun registerSensorResponseCallback(sensorCallback: LtpSensorResponseCallback) {
            ltpSensorResponseCallback.register(sensorCallback)
        }

        @Throws(RemoteException::class)
        override fun unregisterSensorResponseCallback(speechCallback: LtpSensorResponseCallback) {
            ltpSensorResponseCallback.unregister(speechCallback)
        }

        @Throws(RemoteException::class)
        override fun setMiCmd(command: String, data: String) {
            sendMiCmd(command, data)
        }

        @Throws(RemoteException::class)
        override fun registerMiCmdResponseCallback(miCmdCallback: LtpMiCmdCallback) {
            ltpMiCmdCallback.register(miCmdCallback)
        }

        @Throws(RemoteException::class)
        override fun unregisterMiCmdResponseCallback(miCmdCallback: LtpMiCmdCallback) {
            ltpMiCmdCallback.unregister(miCmdCallback)
        }

        @Throws(RemoteException::class)
        override fun setIdentifyCmd(command: String, data: String) {
            sendIdentifyCmd(command, data)
        }

        @Throws(RemoteException::class)
        override fun registerIdentifyCmdCallback(identifyCmdCallback: LtpIdentifyCmdCallback) {
            ltpIdentifyCmdCallback.register(identifyCmdCallback)
        }

        @Throws(RemoteException::class)
        override fun unregisterIdentifyCmdCallback(identifyCmdCallback: LtpIdentifyCmdCallback) {
            ltpIdentifyCmdCallback.unregister(identifyCmdCallback)
        }

        @Throws(RemoteException::class)
        override fun setBleCmd(command: String, data: String, isNeedResponse: Boolean) {
            sendBleCmd(command, data, isNeedResponse)
        }

        @Throws(RemoteException::class)
        override fun registerBleCmdCallback(bleCallback: LtpBleCallback) {
            ltpBleCallback.register(bleCallback)
        }

        @Throws(RemoteException::class)
        override fun unregisterBleCmdCallback(bleCallback: LtpBleCallback) {
            ltpBleCallback.unregister(bleCallback)
        }

        @Throws(RemoteException::class)
        override fun setBleResponse(command: String, data: String) {
            sendBleResponseCmd(command, data)
        }

        @Throws(RemoteException::class)
        override fun registerBleResponseCallback(bleResponseCallback: LtpBleResponseCallback) {
            ltpBleResponseCallback.register(bleResponseCallback)
        }

        @Throws(RemoteException::class)
        override fun unregisterBleResponseCmdCallback(bleResponseCallback: LtpBleResponseCallback) {
            ltpBleResponseCallback.unregister(bleResponseCallback)
        }
    }

    init {
        initHandler()
    }

    private fun responseBleResponse(command: String, data: String) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return
        }
        mBleResponseLock.lock()
        try {
            val N = ltpBleResponseCallback.beginBroadcast()
            if (N > 0) {
                for (i in 0 until N) {
                    try {
                        if (ltpBleResponseCallback.getBroadcastItem(i) != null) {
                            ltpBleResponseCallback.getBroadcastItem(i)!!
                                .onBleCmdResponsReceived(command, data)
                        }
                        Log.e(TAG, "responseBleResponse:=== count:$N  current:$i")
                    } catch (e: Exception) {
                        Log.e(TAG, "responseBleResponse:  异常$i")
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ltpBleResponseCallback.finishBroadcast()
            mBleResponseLock.unlock()
        }
    }

    private fun responseBleCmd(command: String, data: String, isNeedResponse: Boolean) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return
        }
        mBleLock.lock()
        try {
            val N = ltpBleCallback.beginBroadcast()
            if (N > 0) {
                for (i in 0 until N) {
                    try {
                        if (ltpBleCallback.getBroadcastItem(i) != null) {
                            ltpBleCallback.getBroadcastItem(i)!!
                                .onBleCmdReceived(command, data, isNeedResponse)
                        }
                        Log.e(TAG, "responseBleCmd:=== count:$N  current:$i")
                    } catch (e: Exception) {
                        Log.e(TAG, "responseBleCmd:  异常$i")
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ltpBleCallback.finishBroadcast()
            mBleLock.unlock()
        }
    }

    private fun responseIdentifyCmd(command: String, data: String) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return
        }
        mIdentifyLock.lock()
        try {
            val N = ltpIdentifyCmdCallback.beginBroadcast()
            if (N > 0) {
                for (i in 0 until N) {
                    try {
                        if (ltpIdentifyCmdCallback.getBroadcastItem(i) != null) {
                            ltpIdentifyCmdCallback.getBroadcastItem(i)!!
                                .onIdentifyCommandReceived(command, data)
                        }
                        Log.e(TAG, "responseMiCmd:=== count:$N  current:$i")
                    } catch (e: Exception) {
                        Log.e(TAG, "responseMiCmd:  异常$i")
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ltpIdentifyCmdCallback.finishBroadcast()
            mIdentifyLock.unlock()
        }
    }

    private fun responseMiCmd(command: String, data: String) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return
        }
        mMiLock.lock()
        try {
            val N = ltpMiCmdCallback.beginBroadcast()
            if (N == 0) {
                return
            }
            for (i in 0 until N) {
                try {
                    if (ltpMiCmdCallback.getBroadcastItem(i) != null) {
                        ltpMiCmdCallback.getBroadcastItem(i)!!.onMiCommandReceived(command, data)
                    }
                    Log.e(TAG, "responseMiCmd:=== count:$N  current:$i")
                } catch (e: Exception) {
                    Log.e(TAG, "responseMiCmd:  异常$i")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ltpMiCmdCallback.finishBroadcast()
            mMiLock.unlock()
        }
    }

    private fun responseSpeechCmds(command: String, data: String) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return
        }
        mSpeechSLock.lock()
        try {
            val N = ltpSpeechCallback.beginBroadcast()
            if (N == 0) {
                return
            }
            for (i in 0 until N) {
                try {
                    if (ltpSpeechCallback.getBroadcastItem(i) != null) {
                        ltpSpeechCallback.getBroadcastItem(i)!!
                            .onSpeechCommandReceived(command, data)
                    }
                    Log.e(TAG, "setSpeechCmds:=== count:$N  current:$i")
                } catch (e: Exception) {
                    Log.e(TAG, "setSpeechCmds:  异常$i")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ltpSpeechCallback.finishBroadcast()
            mSpeechSLock.unlock()
        }
    }

    private fun responseRobotTTS(command: String, data: String) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return
        }
        mTTSLock.lock()
        try {
            val N = ltpTTSCallback.beginBroadcast()
            if (N == 0) {
                return
            }
            for (i in 0 until N) {
                if (ltpTTSCallback.getBroadcastItem(i) != null) {
                    ltpTTSCallback.getBroadcastItem(i)!!.onTTSCommand(command, data)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ltpTTSCallback.finishBroadcast()
            mTTSLock.unlock()
        }
    }

    private fun responseRobotStatusCmds(command: String, data: String) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return
        }
        mStatusLock.lock()
        try {
            val N = ltpRobotStatusCallback.beginBroadcast()
            if (N == 0) {
                return
            }
            for (i in 0 until N) {
                if (ltpRobotStatusCallback.getBroadcastItem(i) != null) {
                    ltpRobotStatusCallback.getBroadcastItem(i)!!.onRobotStatusChanged(command, data)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ltpRobotStatusCallback.finishBroadcast()
            mStatusLock.unlock()
        }
    }

    private fun responseAppCommand(command: String, data: String) {
        Log.d("-------", "command: $command")
        Log.d("-------", "data: $data")
        synchronized(this) {
            if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
                return
            }
            mAppLock.lock()
            try {
                val N = ltpAppCmdCallback.beginBroadcast()
                if (N > 0) {
                    for (i in 0 until N) {
                        Log.d("-------", "--$N")
                        Log.d("-------", "--$i")
                        if (ltpAppCmdCallback.getBroadcastItem(i) != null) {
                            ltpAppCmdCallback.getBroadcastItem(i)!!
                                .onAppCommandReceived(command, data)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                ltpAppCmdCallback.finishBroadcast()
                mAppLock.unlock()
            }
        }
    }

    private fun responseExpressionChange(command: String, data: String) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return
        }
        mExpressionLock.lock()
        try {
            val N = ltpExpressionCallback.beginBroadcast()
            if (N == 0) {
                return
            }
            for (i in 0 until N) {
                if (ltpExpressionCallback.getBroadcastItem(i) != null) {
                    ltpExpressionCallback.getBroadcastItem(i)!!.onExpressionChanged(command, data)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ltpExpressionCallback.finishBroadcast()
            mExpressionLock.unlock()
        }
    }

    private fun responseAudioEffectCmd(command: String, data: String) {
        Log.e("letianpai_sound0", "==== lettianpaiservice_2")
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return
        }
        Log.e("letianpai_sound0", "==== lettianpaiservice_3")
        mAeLock.lock()
        try {
            val N = ltpAudioEffectCallback.beginBroadcast()
            if (N == 0) {
                return
            }
            for (i in 0 until N) {
                Log.e("letianpai_sound0", "==== lettianpaiservice_4")
                if (ltpAudioEffectCallback.getBroadcastItem(i) != null) {
                    ltpAudioEffectCallback.getBroadcastItem(i)!!.onAudioEffectCommand(command, data)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            Log.e("letianpai_sound0", "==== lettianpaiservice_5")
            ltpAudioEffectCallback.finishBroadcast()
            mAeLock.unlock()
        }
    }

    private fun responseMcuCommand(command: String, data: String) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return
        }
        mMcuLock.lock()
        try {
            val N = ltpMcuCommandCallback.beginBroadcast()
            if (N == 0) {
                return
            }
            for (i in 0 until N) {
                if (ltpMcuCommandCallback.getBroadcastItem(i) != null) {
                    ltpMcuCommandCallback.getBroadcastItem(i)!!.onMcuCommandCommand(command, data)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ltpMcuCommandCallback.finishBroadcast()
            mMcuLock.unlock()
        }
    }

    private fun setLTPRobotStatus(robotStatus: Int) {
        mLock.lock()

        try {
            val N = RemoteCallbackList.beginBroadcast()
            if (N == 0) {
                return
            }
            for (i in 0 until N) {
                if (RemoteCallbackList.getBroadcastItem(i) != null) {
                    RemoteCallbackList.getBroadcastItem(i)!!.onRobotStatusChanged(robotStatus)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            RemoteCallbackList.finishBroadcast()
            mLock.unlock()
        }
    }

    private fun responseCommand(ltpCommand: LtpCommand) {
        mLock.lock()
        try {
            val N = RemoteCallbackList.beginBroadcast()
            if (N == 0) {
                return
            }
            for (i in 0 until N) {
                if (RemoteCallbackList.getBroadcastItem(i) != null) {
                    RemoteCallbackList.getBroadcastItem(i)!!.onCommandReceived(ltpCommand)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            RemoteCallbackList.finishBroadcast()
            mLock.unlock()
        }
    }

    private fun responseLongConnectCommand(command: String, data: String) {
        Log.d("<<<<", "responseLongConnectCommand: command--$command-----data::$data")
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return
        }
        mLongConnectLock.lock()
        try {
            val N = ltpLongConnectCallback.beginBroadcast()
            if (N == 0) {
                return
            }
            for (i in 0 until N) {
                if (ltpLongConnectCallback.getBroadcastItem(i) != null) {
                    ltpLongConnectCallback.getBroadcastItem(i)!!.onLongConnectCommand(command, data)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ltpLongConnectCallback.finishBroadcast()
            mLongConnectLock.unlock()
        }
    }

    private fun responseSensorCommand(command: String, data: String) {
        if (TextUtils.isEmpty(command)) {
            return
        }
        mSensorLock.lock()
        try {
            val N = ltpSensorResponseCallback.beginBroadcast()
            if (N == 0) {
                return
            }
            for (i in 0 until N) {
                if (ltpSensorResponseCallback.getBroadcastItem(i) != null) {
                    ltpSensorResponseCallback.getBroadcastItem(i)!!.onSensorResponse(command, data)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ltpSensorResponseCallback.finishBroadcast()
            mSensorLock.unlock()
        }
    }


    private fun sendLongConnectCmd(cmd: String, data: String) {
        sendRobotCmd(robotLongConnectHandler, RobotAidlConsts.CMD_LONG_CONNECT, CmdInfo(cmd, data))
    }

    private fun sendMcuCmd(cmd: String, data: String) {
        sendRobotCmd(robotMcuHandler, RobotAidlConsts.CMD_MCU, CmdInfo(cmd, data))
    }

    private fun sendAudioEffectCmd(cmd: String, data: String) {
        sendRobotCmd(robotAudioEffectHandler, RobotAidlConsts.CMD_AUDIO_EFFECT, CmdInfo(cmd, data))
    }

    private fun sendExpressionCmd(cmd: String, data: String) {
        sendRobotCmd(robotExpressionHandler, RobotAidlConsts.CMD_EXPRESSION, CmdInfo(cmd, data))
    }

    private fun sendAppCmd(cmd: String, data: String) {
        sendRobotCmd(robotAppCmdHandler, RobotAidlConsts.CMD_APP_CMD, CmdInfo(cmd, data))
    }

    private fun sendRobotStatus(cmd: String, data: String) {
        sendRobotCmd(robotRobotStatusHandler, RobotAidlConsts.CMD_ROBOT_STATUS, CmdInfo(cmd, data))
    }

    private fun sendTTS(cmd: String, data: String) {
        sendRobotCmd(robotTTSHandler, RobotAidlConsts.CMD_TTS, CmdInfo(cmd, data))
    }

    private fun sendSpeechCmd(cmd: String, data: String) {
        sendRobotCmd(robotSpeechCmdHandler, RobotAidlConsts.CMD_SPEECH, CmdInfo(cmd, data))
    }

    private fun sendSensorCmd(cmd: String, data: String) {
        sendRobotCmd(robotSensorHandler, RobotAidlConsts.CMD_SENSOR, CmdInfo(cmd, data))
    }

    private fun sendMiCmd(cmd: String, data: String) {
        sendRobotCmd(robotMiHandler, RobotAidlConsts.CMD_MI, CmdInfo(cmd, data))
    }

    private fun sendIdentifyCmd(cmd: String, data: String) {
        sendRobotCmd(robotIdentifyHandler, RobotAidlConsts.CMD_IDENTIFY, CmdInfo(cmd, data))
    }

    private fun sendBleCmd(cmd: String, data: String, isNeedResponse: Boolean) {
        sendRobotCmd(robotBleHandler, RobotAidlConsts.CMD_BLE, CmdInfo(cmd, data), isNeedResponse)
    }

    private fun sendBleResponseCmd(cmd: String, data: String) {
        sendRobotCmd(robotBleResponseHandler, RobotAidlConsts.CMD_BLE_RESPONSE, CmdInfo(cmd, data))
    }


    private fun sendRobotCmd(handler: Handler?, cmdType: Int, cmdInfo: CmdInfo) {
        val message = Message()
        message.what = cmdType
        message.obj = cmdInfo
        handler!!.sendMessage(message)
    }

    private fun sendRobotCmd(
        handler: Handler?,
        cmdType: Int,
        cmdInfo: CmdInfo,
        isNeedResponse: Boolean
    ) {
        val message = Message()
        message.what = cmdType
        message.obj = cmdInfo
        if (isNeedResponse) {
            message.arg2 = 1
        }
        handler!!.sendMessage(message)
    }


    private inner class RobotLongConnectHandler(context: Context) : Handler() {
        private val context = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == RobotAidlConsts.CMD_LONG_CONNECT) {
                if (msg.obj != null && (msg.obj as CmdInfo) != null) {
                    val command = (msg.obj as CmdInfo).command
                    val data = (msg.obj as CmdInfo).data
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)) {
                        responseLongConnectCommand(command, data)
                    }
                }
            }
        }
    }


    private inner class RobotMcuHandler(context: Context) : Handler() {
        private val context = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == RobotAidlConsts.CMD_MCU) {
                if (msg.obj != null && (msg.obj as CmdInfo) != null) {
                    val command = (msg.obj as CmdInfo).command
                    val data = (msg.obj as CmdInfo).data
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)) {
                        responseMcuCommand(command, data)
                    }
                }
            }
        }
    }

    private inner class RobotAudioEffectHandler(context: Context) : Handler() {
        private val context = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == RobotAidlConsts.CMD_AUDIO_EFFECT) {
                if (msg.obj != null && (msg.obj as CmdInfo) != null) {
                    val command = (msg.obj as CmdInfo).command
                    val data = (msg.obj as CmdInfo).data
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)) {
                        responseAudioEffectCmd(command, data)
                    }
                }
            }
        }
    }

    private inner class RobotExpressionHandler(context: Context) : Handler() {
        private val context = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == RobotAidlConsts.CMD_EXPRESSION) {
                if (msg.obj != null && (msg.obj as CmdInfo) != null) {
                    val command = (msg.obj as CmdInfo).command
                    val data = (msg.obj as CmdInfo).data
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)) {
                        responseExpressionChange(command, data)
                    }
                }
            }
        }
    }


    private inner class RobotAppCmdHandler(context: Context) : Handler() {
        private val context = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.obj != null && (msg.obj as CmdInfo) != null) {
                val command = (msg.obj as CmdInfo).command
                val data = (msg.obj as CmdInfo).data
                if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)) {
                    responseAppCommand(command, data)
                }
            }
        }
    }

    private inner class RobotRobotStatusHandler(context: Context) : Handler() {
        private val context = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == RobotAidlConsts.CMD_ROBOT_STATUS) {
                if (msg.obj != null && (msg.obj as CmdInfo) != null) {
                    val command = (msg.obj as CmdInfo).command
                    val data = (msg.obj as CmdInfo).data
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)) {
                        responseRobotStatusCmds(command, data)
                    }
                }
            }
        }
    }

    private inner class RobotTTSHandler(context: Context) : Handler() {
        private val context = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == RobotAidlConsts.CMD_TTS) {
                if (msg.obj != null && (msg.obj as CmdInfo) != null) {
                    val command = (msg.obj as CmdInfo).command
                    val data = (msg.obj as CmdInfo).data
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)) {
                        responseRobotTTS(command, data)
                    }
                }
            }
        }
    }


    private inner class RobotSpeechCmdHandler(context: Context) : Handler() {
        private val context = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == RobotAidlConsts.CMD_SPEECH) {
                if (msg.obj != null && (msg.obj as CmdInfo) != null) {
                    val command = (msg.obj as CmdInfo).command
                    val data = (msg.obj as CmdInfo).data
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)) {
                        responseSpeechCmds(command, data)
                    }
                }
            }
        }
    }


    private inner class RobotSensorHandler(context: Context) : Handler() {
        private val context = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == RobotAidlConsts.CMD_SENSOR) {
                if (msg.obj != null && (msg.obj as CmdInfo) != null) {
                    val command = (msg.obj as CmdInfo).command
                    val data = (msg.obj as CmdInfo).data
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)) {
                        responseSensorCommand(command, data)
                    }
                }
            }
        }
    }

    private inner class RobotMiHandler(context: Context) : Handler() {
        private val context = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == RobotAidlConsts.CMD_MI) {
                if (msg.obj != null && (msg.obj as CmdInfo) != null) {
                    val command = (msg.obj as CmdInfo).command
                    val data = (msg.obj as CmdInfo).data
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)) {
                        responseMiCmd(command, data)
                    }
                }
            }
        }
    }


    private inner class RobotIdentifyHandler(context: Context) : Handler() {
        private val context = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == RobotAidlConsts.CMD_IDENTIFY) {
                if (msg.obj != null && (msg.obj as CmdInfo) != null) {
                    val command = (msg.obj as CmdInfo).command
                    val data = (msg.obj as CmdInfo).data
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)) {
                        responseIdentifyCmd(command, data)
                    }
                }
            }
        }
    }

    private inner class RobotBleHandler(context: Context) : Handler() {
        private val context = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            if (msg.what == RobotAidlConsts.CMD_BLE) {
                if (msg.obj != null && (msg.obj as CmdInfo) != null) {
                    val command = (msg.obj as CmdInfo).command
                    val data = (msg.obj as CmdInfo).data
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)) {
                        if (msg.arg2 == 1) {
                            responseBleCmd(command, data, true)
                        } else {
                            responseBleCmd(command, data, false)
                        }
                    }
                }
            }
        }
    }

    private inner class RobotBleResponseHandler(context: Context) : Handler() {
        private val context = WeakReference(context)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == RobotAidlConsts.CMD_BLE_RESPONSE) {
                if (msg.obj != null && (msg.obj as CmdInfo) != null) {
                    val command = (msg.obj as CmdInfo).command
                    val data = (msg.obj as CmdInfo).data
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)) {
                        responseBleResponse(command, data)
                    }
                }
            }
        }
    }


    companion object {
        private const val TAG = "LetianpaiService"
    }
}
