# Geohash

A Clojure library for encoding/decoding	[geohashes](http://en.wikipedia.org/wiki/Geohash) with performance comparable to the [reference Java implementation](https://github.com/kungfoo/geohash-java).

## Usage

For Leiningen, add this dependency to your project:
   
    [org.clojars.dbasch/geohash "0.1.0"] 

Encoding and decoding geohashes:

```clojure
(require '[geohash.core :as geohash])

(geohash.core/encode 45.6789 34.5678)
=> "ub0efeedbh5u"

(geohash/decode "ub0efeedbh5u")
=> [45.67889937199652 34.56779899075627]
```
## License

Copyright © 2014 Diego Basch

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
