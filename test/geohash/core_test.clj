(ns geohash.core-test
  (:require [clojure.test :refer :all]
            [geohash.core :refer :all]))

(defn random-point
  []
  (let [lat (/
             (- (* 90 1000 1000)
                (rand-int (* 180 1000 1000)))
             1000000.0)
        lng (/
             (- (* 180 1000 1000)
                (rand-int (* 360 1000 1000)))
             1000000.0)]
    [lat lng]))

(defn test-round-trip [n]
  (let [[lat lng] (random-point)
        [lat2 lng2] (decode (encode lat lng n))
        d1 (- lat2 lat)
        d2 (- lng2 lng)
        precision (Math/sqrt (+ (* d2 d2) (* d1 d1)))]
    precision))


(deftest test-encode []
  (is (= "7zzzzzzzzzzz" (encode 0.0 0.0)))
  (is (= "mjw5tcr4kdqj" (encode -13.44 53.7)))
  (is (= "v007z1zzh1h1" (encode 45.67 45.67)))
  (is (= "ppcwsv7" (encode -45.2343 137.3234343 7))))

(deftest test-precision
  []
  (dotimes [n 10000]
    (is (< (test-round-trip 12) 2e-6))
    (is (< (test-round-trip 10) 2e-4))
    (is (< (test-round-trip 7) 0.02))
    (is (< (test-round-trip 5) 0.4))))

