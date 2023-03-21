;
; Copyright (c) 2023 See AUTHORS file.
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;   http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;

;; meant to be pasted into a REPL
(def fnt (slurp "GoNotoUniversal-sdf-large.fnt"))
(doseq [n [4.0]] (spit "GoNotoUniversal-sdf0.fnt" (clojure.string/replace (clojure.string/replace fnt #" +" " ") #" x=([\-0-9]+) y=([\-0-9]+) width=([\-0-9]+) height=([\-0-9]+) xoffset=([\-0-9]+) yoffset=([\-0-9]+) xadvance=([\-0-9]+)" (fn[[_ x y w h xo yo xa]] (str " x="(/ (+ (read-string x) (/ n 2)) n) " y="(/ (+ (read-string y) (/ n 2)) n) " width="(/ (+ (read-string w) (/ n 2)) n) " height="(/ (+ (read-string h) (/ n 2)) n) " xoffset="(/ (+ (read-string xo) (/ n 2)) n) " yoffset="(/ (+ (read-string yo) (/ n 2)) n) " xadvance="(/ (+ (read-string xa) (/ n 2)) n) )))))
(def fnt (slurp "GoNotoUniversal-sdf0.fnt"))
(doseq [n [4.0]] (spit "GoNotoUniversal-sdf.fnt" (clojure.string/replace (clojure.string/replace fnt #"(kerning .+amount=)([\-0-9]+) \R" (fn [[_ r a]] (let [calc (/ (+ (read-string a)) n)] (if (zero? calc) "" (str r calc " \n"))))) #"\.0\b" "")))
