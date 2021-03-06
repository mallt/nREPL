(ns nrepl.cmdline-test
  {:author "Chas Emerick"}
  (:require
   [clojure.test :refer :all]
   [nrepl.core :as nrepl]
   [nrepl.core-test :refer [def-repl-test repl-server-fixture *server*]]))

(use-fixtures :once repl-server-fixture)

(comment  ;TODO
  (def-repl-test ack
    (nrepl/reset-ack-port!)
    (let [server-process (.exec (Runtime/getRuntime)
                                (into-array ["java" "-Dnreplacktest=y" "-cp" (System/getProperty "java.class.path")
                                             "nrepl.main" "--ack" (str (:port *server*))]))
          acked-port (nrepl/wait-for-ack 20000)]
      (try
        (is acked-port "Timed out waiting for ack")
        (when acked-port
          (with-open [c2 (nrepl/connect acked-port)]
          ;; just a sanity check
            (is (= "y" (-> (((:send c2) "(System/getProperty \"nreplacktest\")")) nrepl/read-response-value :value)))))
        (finally
          (.destroy server-process)))))

  (def-repl-test explicit-port-argument
    (nrepl/reset-ack-port!)
    (let [free-port (with-open [ss (java.net.ServerSocket.)]
                      (.bind ss nil)
                      (.getLocalPort ss))
          server-process (.exec (Runtime/getRuntime)
                                (into-array ["java" "-Dnreplacktest=y" "-cp" (System/getProperty "java.class.path")
                                             "nrepl.main" "--port" (str free-port) "--ack" (str (:port *server*))]))
          acked-port (nrepl/wait-for-ack 20000)]
      (try
        (is acked-port "Timed out waiting for ack")
        (is (= acked-port free-port))
        (finally
          (.destroy server-process))))))
