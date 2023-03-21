;; meant to be pasted into a REPL
(def fnt (slurp "GoNotoUniversal-sdf-large.fnt"))
(doseq [n [4]] (spit "GoNotoUniversal-sdf0.fnt" (clojure.string/replace fnt #" x=([\-0-9]+) y=([\-0-9]+) width=([\-0-9]+) height=([\-0-9]+) xoffset=([\-0-9]+) yoffset=([\-0-9]+) xadvance=([\-0-9]+)" (fn[[_ x y w h xo yo xa]] (str " x="(quot (+ (read-string x) (/ n 2)) n) " y="(quot (+ (read-string y) (/ n 2)) n) " width="(quot (+ (read-string w) (/ n 2)) n) " height="(quot (+ (read-string h) (/ n 2)) n) " xoffset="(quot (+ (read-string xo) (/ n 2)) n) " yoffset="(quot (+ (read-string yo) (/ n 2)) n) " xadvance="(quot (+ (read-string xa) (/ n 2)) n) )))))
(def fnt (slurp "GoNotoUniversal-sdf0.fnt"))
(doseq [n [4]] (spit "GoNotoUniversal-sdf.fnt" (clojure.string/replace fnt #"(kerning .+amount=)([\-0-9]+) \n" (fn [[_ r a]] (let [calc (int (quot (+ (read-string a) (/ n (Math/copySign 2.0 (Double/parseDouble a)))) n))] (if (zero? calc) "" (str r calc " \n")))))))
