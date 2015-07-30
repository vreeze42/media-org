(ns media-org.views.layout
  (:require [hiccup.page :refer [html5 include-css]]))

(defn common [& body]
  (html5
    [:head
     [:title "Welcome to media-org"]
     (include-css "/css/screen.css")]
    [:body body]))
