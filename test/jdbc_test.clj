(ns jdbc-test
  (:require [jdbc :refer :all]
            [clojure.test :refer :all]))

(def h2-dbspec1 {:classname "org.h2.Driver"
                 :subprotocol "h2"
                 :subname "jdbctest.db"})

(def h2-dbspec2 {:subprotocol "h2"
                 :subname "jdbctest.db"})

(def h2-dbspec3 {:subprotocol "h2"
                 :subname "mem:"})

(deftest db-specs
  (testing "Create connection with distinct dbspec"
    (let [c1 (make-connection h2-dbspec1)
          c2 (make-connection h2-dbspec2)
          c3 (make-connection h2-dbspec3)]
      (is (instance? jdbc.Connection c1))
      (is (instance? jdbc.Connection c2))
      (is (instance? jdbc.Connection c3))))

  (testing "Using macro with-connection"
    (with-connection h2-dbspec3 conn
      (is (instance? jdbc.Connection conn)))))

(deftest db-commands
  (testing "Simple create table"
    (with-connection h2-dbspec3 conn
      (let [sql "CREATE TABLE foo (name varchar(255), age integer);"
            r   (execute! conn sql)]
        (is (= (list 0) r)))))

  (testing "Create duplicate table"
     (with-connection h2-dbspec3 conn
       (let [sql "CREATE TABLE foo (name varchar(255), age integer);"]
         (execute! conn sql)
         (is (thrown? org.h2.jdbc.JdbcBatchUpdateException (execute! conn sql))))))

  (testing "Simple query result"
    (with-connection h2-dbspec3 conn
      (with-query conn results ["SELECT 1 + 1 as foo;"]
        (is (= [{:foo 2}] (doall results))))))

  (testing "Low level query result"
    (with-open [conn    (make-connection h2-dbspec3)
                result  (make-query conn ["SELECT 1 + 1 as foo;"])]
      (is (instance? jdbc.QueryResult result))
      (is (instance? java.sql.ResultSet (:rs result)))
      (is (instance? java.sql.PreparedStatement (:stmt result)))
      (is (= [{:foo 2}] (doall (:data result)))))))
