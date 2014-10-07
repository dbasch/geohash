(ns geohash.core
  (:gen-class)
  (:require clojure.set))

(set! *unchecked-math* true)

(def alphabet (char-array "0123456789bcdefghjkmnpqrstuvwxyz"))

(defmacro ^long decode-char [c]
  (let [clauses (interleave (map byte alphabet) (range))]
    `(case ~c ~@clauses)))

(defn long-to-hash
  "Converts a long to a geohash string."
  [^long n ^long size]
  (let [sb (StringBuilder.)]
    (loop [r n
           s size]
      (if (zero? s)
        (.toString (.reverse sb))
        (do (.append sb (aget ^chars alphabet (bit-and r 2r11111)))
            (recur
             (quot r 32)
             (dec s)))))))

(defn locate
  "Returns the encoded number for the smallest interval containing the
  location represented in b bits."
  [^double lo ^double hi ^double loc ^long mask]
  (loop [m (bit-shift-right mask 1)
         l lo
         h hi
         res 0]
    (if (zero? m)
      res
      (let [med (/ (+ h l) 2)]
        (if (<= loc med)
          (recur (bit-shift-right m 1)
                 l
                 med
                 res)
          (recur (bit-shift-right m 1)
                 med
                 h
                 (bit-or res m)))))))

(defn inter
  "Interleaves x and y into a single number, with the bits of x on the
  odd bits of the result."
  [^long x ^long y]
  (loop [res 0
         b 0]
    (let [res (if (clojure.lang.Numbers/testBit x b) (clojure.lang.Numbers/flipBit res (inc (* b 2))) res)
          res (if (clojure.lang.Numbers/testBit y b) (clojure.lang.Numbers/flipBit res (* b 2)) res)]
      (if (= 30 b)
        res
        (recur
         res
         (inc b))))))

(defn ^String encode
  "Encodes a location into a geohash. If size is not specified, it defaults to 12."
  ([^double lat ^double lng]
     (encode lat lng 12))
  ([^double lat ^double lng ^long size]
     (let [mask (bit-shift-left 1 (quot (* 5 size) 2))
           lng-mask (if (odd? size) (bit-shift-left mask 1) mask)
           lat-num (locate -90.0 90.0 lat mask)
           lng-num (locate -180.0 180.0 lng lng-mask)
           n (if (odd? size)
               (inter lat-num lng-num)
               (inter lng-num lat-num))]
       (long-to-hash n size))))

(defn ^long hash-to-long
  "Converts a geohash string into a long."
  [^bytes s ^long len]
  (let [c (dec len)]
    (loop [res 0
           i c]
      (if (pos? i)
        (recur (bit-shift-left (bit-or (decode-char (aget s (- c i))) res) 5)
               (dec i))
        res))))

(defn deinter
  "Reverses the result of inter."
  [^long n]
  (loop [res1 0
         res2 0
         i 1]
    (if (< i 60)
      (recur 
       (if (clojure.lang.Numbers/testBit n i)
         (clojure.lang.Numbers/flipBit res1 (bit-shift-right (dec i) 1))
         res1)
       (if (clojure.lang.Numbers/testBit n (dec i))
         (clojure.lang.Numbers/flipBit res2 (bit-shift-right (dec i) 1))
         res2)
       (+ 2 i))
      [res1 res2])))

(defn ^double subdivide
  "Finds the closest double to an encoded long dimension."
  [^double lo ^double hi ^long n ^long bits]
  (loop [b (dec bits)
         l lo
         h hi]
    (if (neg? b)
      (/ (+ l h) 2)
      (let [med (/ (+ h l) 2)]
        (if (clojure.lang.Numbers/testBit n b)
          (recur (dec b)
                 med
                 h)
          (recur (dec b)
                 l
                 med))))))

(defn decode
  "Decodes a geohash into a latitude / longitude pair."
  [^String s]
  (let [len (.length s)
        lng-lat (-> (.getBytes s) (hash-to-long len) (deinter))
        [lng-num lat-num] (if (odd? len)
                            (reverse lng-lat)
                            lng-lat)
        lat-bits (long (bit-shift-right (* 5 len) 1))
        lng-bits (if (odd? len) (inc lat-bits) lat-bits)
        lat (subdivide -90.0 90.0 lat-num lat-bits)
        lng (subdivide -180.0 180.0 lng-num lng-bits)]
    [lat lng]))