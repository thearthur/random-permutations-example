(ns random-permutations-example.core
  (:use [incanter core stats charts]))

(def foo (transient [1 2 3 4]))

(defn swap [v a b]
  (let [tmp (nth v a)]
    (assoc! v a (nth v b))
    (assoc! v b tmp)))

(defn rand-range [low high]
  (+ low (rand-int (inc (- high low)))))

(defn permute-randomly [v]
  (let [trans-v (transient v)
        len (count v)]
    (doseq [i (range len)]
      (swap trans-v i (better-rand-range i (dec len))))
    (persistent! trans-v)))

(defn permute-un-randomly [v]
  (let [trans-v (transient v)
        len (count v)]
    (doseq [i (range len)]
      (swap trans-v i (rand-range 0 (dec len))))
    (persistent! trans-v)))

(defn distribution [f]
  (->> (repeatedly f)
       (take 1000000)
       frequencies
       (sort-by first)))

(def unrandom-permutations (distribution #(permute-un-randomly [1 2 3 4])))
([[1 2 3 4] 38870] [[1 2 4 3] 39422] [[1 3 2 4] 39014] [[1 3 4 2] 54741]
 [[1 4 2 3] 42951] [[1 4 3 2] 35430] [[2 1 3 4] 38947] [[2 1 4 3] 58458]
 [[2 3 1 4] 54653] [[2 3 4 1] 54620] [[2 4 1 3] 42609] [[2 4 3 1] 42903]
 [[3 1 2 4] 43182] [[3 1 4 2] 42971] [[3 2 1 4] 35300] [[3 2 4 1] 42829]
 [[3 4 1 2] 43057] [[3 4 2 1] 39272] [[4 1 2 3] 31322] [[4 1 3 2] 35164]
 [[4 2 1 3] 35280] [[4 2 3 1] 31272] [[4 3 1 2] 38844] [[4 3 2 1] 38889])

(def random-permutations (distribution #(permute-randomly [1 2 3 4])))
([[1 2 3 4] 41575] [[1 2 4 3] 41581] [[1 3 2 4] 41637] [[1 3 4 2] 41952]
 [[1 4 2 3] 41795] [[1 4 3 2] 41810] [[2 1 3 4] 41752] [[2 1 4 3] 41720]
 [[2 3 1 4] 41735] [[2 3 4 1] 41658] [[2 4 1 3] 41251] [[2 4 3 1] 41851]
 [[3 1 2 4] 41721] [[3 1 4 2] 41992] [[3 2 1 4] 41690] [[3 2 4 1] 41382]
 [[3 4 1 2] 42152] [[3 4 2 1] 41560] [[4 1 2 3] 41364] [[4 1 3 2] 41732]
 [[4 2 1 3] 41595] [[4 2 3 1] 41441] [[4 3 1 2] 41177] [[4 3 2 1] 41877])

(def permute-un-randomly-dist  (map second unrandom-permutations))
(def permute-randomly-dist  (map second random-permutations))
(sd permute-un-randomly-dist)
(sd permute-randomly-dist)

(pprint (map #(vector (% permute-randomly-dist)
                      (% permute-un-randomly-dist))
             [mean median sd variance skewness kurtosis]))

(def plot
  (bar-chart (map first unrandom-permutations)
             (map second unrandom-permutations)
             :series-label "Random swap anywhere"
             :legend true
             :x-label "Permutation"
             :y-label "Occurrances of permutation"
             :title "Distribution of 1,000,000 permutations"))

(add-categories plot
                (map first random-permutations)
                (map second random-permutations)
                :series-label "Random swap after point")