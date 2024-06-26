### Build 
```bash
./mvnw clean install 
```

### About it

Stock exchange application written in java 19. 
Imagine you want to be notified when something happens, some GOOGLE(GOOG) stock went up or down.
There are some simple rules(Equal, GreaterThan,LessThan) when the stock price change. Fake Data generation techniques are used to generate a lot of data for benchmarks.
Such solution would be used for Day Trading applications.

### Design
<div align="center" >
<img src="UML.drawio.png" width="800" height="800" ></img><br/>
 **Interfaces was duplicated to make the diagram more clean.
</div>

UML in Colors scheme:
* Green: Interfaces
* Blue: Predicates (a.k.a rules)
* Purple: Fake data generation
* Dark blue: Matching engine
* Yellow: Raw events
* Red: Main class, main orchestration of the program and benchmarks

### Rationale

OMG Diego you lost your mind, no not really. 
This is quadratic time(for inside for), I know, but this is very useful because allows to think in the worst case scenario.

Let's recap: Single Threaded, no JVM tuning, poorly implemented, no tuning, ~10-30% CPU, ~16GB ram:
 * Even on this case 10k events running against 10k rules took only 1.8 seconds is pretty fast for worst case(10kx10k).
 * Sure 100k starts to get slow, but for something in the background, 3.8 minutes is not bad!(100kx100k) :-) 
 * We can do better, there are opportunities to improve

### Benchmark
```bash
Matching 10 events/predicates resulted in: [4] match in 0 ms
Matching 100 events/predicates resulted in: [242] match in 1 ms
Matching 1000 events/predicates resulted in: [34640] match in 47 ms
Matching 10000 events/predicates resulted in: [3143928] match in 1898 ms
Matching 100000 events/predicates resulted in: [313618905] match in 231192 ms (3.8 minutes)
```

### How can we optimize and scale

1. Start filtering the rules by users(uuid, id, hash, or symbol, whatever).
2. CAP how many rules a user can have lets say 100 is max.
3. Use thread pool, process all in parallel rather a single thread, is all cpu bound (2 threads per core).
4. Simple things, integers instead of strings for symbols, it would speed up things and use less memory. 
5. More machines with some seeding or light coordination(zookeeper like).
6. Instead of processing all at once, make continuous processing, as the events arrive you process.
7. Optimize the JVM with proper flags for GC and pre-alocate memory
8. Optimized languages like Go, Rust or Zig.

### CAP 100 rules

Now we can do:
 * 100k in 155 ms
 * 1M in 1 second
 * 10M in 10 seconds

Again, single machine, still have 7 other optimizations to play.

```bash
>> Benchmarks: CAP 100 rules 
Matching 10 events / 100 predicates resulted in: [33] match in 0 ms
Matching 100 events / 100 predicates resulted in: [253] match in 1 ms
Matching 1000 events / 100 predicates resulted in: [3229] match in 8 ms
Matching 10000 events / 100 predicates resulted in: [27412] match in 55 ms
Matching 100000 events / 100 predicates resulted in: [303540] match in 155 ms
Matching 1000000 events / 100 predicates resulted in: [3273769] match in 1090 ms
Matching 10000000 events / 100 predicates resulted in: [30800135] match in 10483 ms
```