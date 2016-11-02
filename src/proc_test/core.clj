(ns proc-test.core
  (:import [org.apache.commons.exec
            PumpStreamHandler
            DefaultExecutor
            CommandLine
            ExecuteException
            AutoFlushingPumpStreamHandler
            AutoFlushingStreamPumper])
  (:require [clojure.core.async :as async :refer [<! go-loop]]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn -main []
  (let [ex (DefaultExecutor.)
        cl (CommandLine. "ipython")
        istream (java.io.PipedInputStream.)
        ostream (java.io.PipedOutputStream. istream)

        channel (async/chan)
        
        stdout-handler (proxy [org.apache.commons.exec.LogOutputStream] [] 
                         (processLine
                           [line]
                           (async/>!! channel "in stdout")
                           (println (format "+++ OUT: %s" line))))
        
        stderr-handler (proxy [org.apache.commons.exec.LogOutputStream] [] 
                         (processLine
                           [line] 
                           (async/>!! channel "in stderr")
                           (println (format "!!! ERR: %s" line))))]
    
    (def mytimer
      (let [mytimer (java.util.Timer.)
            task (proxy [java.util.TimerTask] []
                   (run []
                     (println "now" (System/currentTimeMillis))
                     (async/>!! channel "in timer task")
                     (.write ostream (.getBytes (str "import time;print('python', time.time())\n\n") "UTF-8"))
                     (.flush ostream)
                     ))
            ]
        (.schedule mytimer task 0 1000)
        mytimer))
    (let [canceler (java.util.Timer.)
          task (proxy [java.util.TimerTask] []
                 (run []
                   (println "TERMINATING at " (System/currentTimeMillis))
                   (async/close! channel)
                   (.write ostream (.getBytes "exit()" "UTF-8"))
                   (.flush ostream)
                   (.cancel mytimer)
                   (.purge mytimer)
                   (.close ostream)))]
      (.schedule canceler task 4000))

    (async/thread
      (loop []
        (when-some [s (async/<!! channel)]
          (println ">>> CHN:" s)
          (recur))))

    (.setStreamHandler
     ex (AutoFlushingPumpStreamHandler.
         stdout-handler
         stderr-handler
         istream))
    (.addArguments cl "-i")
    (try
      (.execute ex cl)
      (catch ExecuteException e
        (.printStackTrace e))
      (catch java.io.IOException e
        (.printStackTrace e)))))
