(defproject proc-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.apache.commons/commons-exec "1.3"]
                 [org.clojars.hozumi/clj-commons-exec "1.2.0"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 ]
  :java-source-paths ["src/java"]
  :repl-options {:init-ns proc-test.core}
  :main proc-test.core)
