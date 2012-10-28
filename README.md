# Off by One 
## It starts with a snarky remark...

I have been reading [The Algorithm Design manual]
(http://www.algorist.com/) and really enjoying it. It's written in a
flowing style and the tone is conversational enough to hold my
interests, though it is still has a bit of a textbookish feel (some
people enjoy that). The first half of the book took a while and I must
admit to some potential lack of retention, it was sufficiently
comprehensive. The second half is going quite quickly because it is
broken down into independent arthur-attention-span sized chunks
designed to build a good mental catalog of what is possible. In
section 14.4 Professor Skiena makes the snarky quip that:


> "Generating random permutations is an important little problem that
 people often stumble across, and often botch up. The right way it to
 use the following two line, linear-time algorithm". 


Which I have translated into Clojure:

```clojure
(defn permute-randomly [v]
  (let [trans-v (transient v)
        len (count v)]
    (doseq [i (range len)]
      (swap trans-v i (rand-range i (dec len))))
    (persistent! trans-v)))
```

Okay, not too hard to understand, you just go through the vector and
swap each element with one of the others. Skiena is a smart guy, I'll
trust him to get at least that much correct. The next paragraph made
me laugh out loud right there in Lulu Carpenters Coffee Shop:

> "That this algorithm generates all permutations uniformly at random is
 not obvious. If you think so, convincingly explain the the following
 algorithm does not generate permutations uniformly"

Apparently I'm good at convincing my self I understand things that
actually require a bit more thinking. It took a couple readings to
even spot the difference (i'll give it away, he swaps an "i" for a
"0").  I have reproduced his code in Clojure again: 

```clojure
(defn permute-un-randomly [v]
  (let [trans-v (transient v)
        len (count v)]
    (doseq [i (range len)]
      (swap trans-v i (rand-range 0 (dec len))))
    (persistent! trans-v)))
```

So the first algorithm swaps each element with one of the elements
after it, and the second one swaps each element with a totally random
element. 

## Prove it!

Okay, that looks fairly similar and also looks like it should work...
Again I'm going to trust Mr. Skiena and believe that this is not
producing proper distributions. So I ran  each of them 1,000,000 times
and counted the number of times each result is generated. It seems
reasonable to expect them to be roughly the same for the proper
version and slightly off, perhaps favoring one, on the improper
version. I mean, it's only a little different so it should be only a
little off right? Lets break out the stats!

```clojure
(defn distribution [f]
  (->> (repeatedly f)
       (take 1000000)
       frequencies
       (sort-by first)))

(def unrandom-permutations (distribution #(permute-un-randomly [1 2 3 4])))
(def random-permutations (distribution #(permute-randomly [1 2 3 4])))
```
<table>
<tr> <td>permutation</td> <td>permute-randomly</td>  <td>permute-un-randomly</td><td>&nbsp;</td><td>permutation</td> <td>permute-randomly</td>  <td>permute-un-randomly</td> </tr>
<tr><td> [1 2 3 4]</td> <td> 41575</td>   <td>38870</td> <td>&nbsp;</td><td> [3 1 2 4]</td> <td> 41721</td>   <td>43182</td></tr>
<tr><td> [1 2 4 3]</td> <td> 41581</td>   <td>39422</td> <td>&nbsp;</td><td> [3 1 4 2]</td> <td> 41992</td>   <td>42971</td></tr>
<tr><td> [1 3 2 4]</td> <td> 41637</td>   <td>39014</td> <td>&nbsp;</td><td> [3 2 1 4]</td> <td> 41690</td>   <td>35300</td></tr>
<tr><td> [1 3 4 2]</td> <td> 41952</td>   <td>54741</td> <td>&nbsp;</td><td> [3 2 4 1]</td> <td> 41382</td>   <td>42829</td></tr>
<tr><td> [1 4 2 3]</td> <td> 41795</td>   <td>42951</td> <td>&nbsp;</td><td> [3 4 1 2]</td> <td> 42152</td>   <td>43057</td></tr>
<tr><td> [1 4 3 2]</td> <td> 41810</td>   <td>35430</td> <td>&nbsp;</td><td> [3 4 2 1]</td> <td> 41560</td>   <td>39272</td></tr>
<tr><td> [2 1 3 4]</td> <td> 41752</td>   <td>38947</td> <td>&nbsp;</td><td> [4 1 2 3]</td> <td> 41364</td>   <td>31322</td></tr>
<tr><td> [2 1 4 3]</td> <td> 41720</td>   <td>58458</td> <td>&nbsp;</td><td> [4 1 3 2]</td> <td> 41732</td>   <td>35164</td></tr>
<tr><td> [2 3 1 4]</td> <td> 41735</td>   <td>54653</td> <td>&nbsp;</td><td> [4 2 1 3]</td> <td> 41595</td>   <td>35280</td></tr>
<tr><td> [2 3 4 1]</td> <td> 41658</td>   <td>54620</td> <td>&nbsp;</td><td> [4 2 3 1]</td> <td> 41441</td>   <td>31272</td></tr>
<tr><td> [2 4 1 3]</td> <td> 41251</td>   <td>42609</td> <td>&nbsp;</td><td> [4 3 1 2]</td> <td> 41177</td>   <td>38844</td></tr>
<tr><td> [2 4 3 1]</td> <td> 41851</td>   <td>42903</td> <td>&nbsp;</td><td> [4 3 2 1]</td> <td> 41877</td>   <td>38889</td></tr>
</table>

OK, the allegedly correct algorithm does look a little more uniform,
though it's a bit hard to really judge from looking at a big table of
numbers. [Incanter](http://incanter.org/) is a wonderful R-like library for
Clojure which provides in my marginally humble opinion a much more
palatable syntax than actually using R (sorry, R-fans, don't
leave...). Incanter provides a bunch of useful functions for 
summarizing long lists of numbers. 

```clojure
(pprint (map #(vector (% permute-randomly-dist)
                      (% permute-un-randomly-dist))
             [mean median sd]))
````
<table>
<tr><td>&nbsp;</td> <td>permute-randomly</td> <td>permute-un-randomly</td></tr>
<tr><td>mean</td>          <td>41666.67</td>   <td> 41666.67</tr></tr>
<tr><td>median</td>        <td> 41705.0</td>   <td>  39347.0</tr></tr>
<tr><td>Standard Deviation</td><td>231.25</td> <td>  7303.80</tr></tr>
</table>

A 3,150% difference in standard deviation sounds like convincing
evidence that something really is wrong with the more intuitive method
of randomly swapping each element with any other element. Lets take a
look at what these differences really look like:

![distribution graph](random-permutations-example/raw/master/resources/dist.png)

## But Why?


## License
Copyright © 2012 Arthur Ulfeldt

Distributed under the Eclipse Public License, the same as Clojure.
