(ns clojure-static-linter.core
  (:require wall.hack)
  (:gen-class))

(defn read-strings
  "Returns a sequence of forms read from string."
  ([string]
   (read-strings []
                 (-> string (java.io.StringReader.)
                   (clojure.lang.LineNumberingPushbackReader.))))
  ([forms reader]
   (let [form (clojure.lang.LispReader/read reader false ::EOF false)]
     (if (= ::EOF form)
       forms
       (recur (conj forms form) reader)))))

(defn format-syntax-error
  "Rewrites clojure.lang.LispReader$ReaderException to have error messages that
  might actually help someone."
  ([e] (format-syntax-error e "(no file)"))
  ([e file]
   ; Lord help me.
   (let [line (wall.hack/field (class e) :line e)
         msg (.getMessage (or (.getCause e) e))]
    (str "(" file ":" line ") " msg))))

(defn validate-config
  "Check that a config file has valid syntax."
  [file]
  (try
    (read-strings (slurp file))
    (catch clojure.lang.LispReader$ReaderException e
      (println (format-syntax-error e file)))))

(defn -main
  [& args]
  (validate-config (first args)))
