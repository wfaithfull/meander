![alt text](meander-logo.png "Meander Logo")

> A library for functional change detection, and change detector evaluation. A byproduct of the PhD research of [Will Faithfull](https://will.faithfull.me).

## Functional Change Detector Pipelines

Modern statistical change detection approaches for multivariate data are often complex pipelines of operations, with each
step dependent on the output of the last. Meander formalises this concept into an API, with the intention of enabling
rapid prototyping of changes and minimal reproduction of code.

Ultimately, a streaming change detector processes examples from a stream one at a time, each time producing a boolean
value indicating whether it believes change has occurred.

The fundamental library component of change detection is called a `Pipe`, and this encapsulates a function from an input 
type to an output type. Therefore, the types for univariate and multivariate change detection are

- `Pipe<Double, Boolean>`
- `Pipe<Double[], Boolean>`

### Composition

A `Pipe` can be attached to another pipe, providing the output and input types match. This means that a `Pipe<Double[], Boolean>`
might actually be composed of multiple pipes, with the output of each placed into the input of the next.

```
Pipe<Double[], Double> reduction = ...
Pipe<Double, Boolean> decision  = ...

Pipe<Double[], Boolean> detector = reduction.then(decision);

Double[] example = ...

Boolean wasChangeDetected = detector.execute(example);
```

This library offers basic building blocks with which to build some common change detection approaches in this pattern.
For example, a detector which uses Hotelling's T-squared test on adjacent time-windows of data can be created as follows:

```
Pipe<Double[], Boolean> tsq = new WindowPairPipe(100)
	.then(new TSquared())
	.then(new FWithDF().complementary())
	.then(Threshold.lessThan(0.05));
```

This pipeline

- Populates a pair of sliding windows (50,50).
- Calculates the Hotelling's T-squared statistic between their elements.
- Finds the statistic's p-value on the cumulative distribution function of an F distribution.
- Takes the complementary probability (1-p)
- Signals change if the result is less than 0.05.

This amounts to the "Hotelling" detector from [1]. Significantly, we can easily add or replace steps in this pipeline without any reengineering. Below, we assess the impact of a PCA feature extraction step:

```
Pipe<Double[], Boolean> hotelling = new WindowPairPipe(100)
	.then(new PCAWindowPairTransform())
	.then(new TSquared())
	.then(new FWithDF().complementary())
    .then(Threshold.lessThan(0.05));
```

## Evaluation

A simple evaluation framework is offered out-of-the-box, which calculates the standard change detection performance indicators:

| Indicator        | Meaning           | Ideal Value  |
| ------------- |:-------------:| -----:|
| ARL      | Average Run Length | Subject to dataset |
| TTD     | Time To Detection      |   0 |
| FAR | False Alarm Rate    |    0 |
| MDR | Missed Detection Rate    |    0 |

An evaluator is used as follows:

```
Pipe<Double[], Boolean> detector = ...
Stream<Example> stream = ...
	
Evaluator evaluator = new SequenceEvaluator();
Evaluation results = evaluator.evaluate(detector, stream);

double arl,ttd,far,mdr;
arl = results.getArl();
ttd = results.getTtd();
far = results.getFar();
mdr = results.getMdr();
```

The `SequenceEvaluator` treats every concept boundary as a change to be detected. The `ShortConceptsEvaluator` only treats transitions *to* the change concept as changes to be detected. This is ideal for problems where changes are extremely short-lived, such as eye-blink detection.

## Change Stream Generation

Meander offers generation of concept drifting data streams using the Java 8 `Stream<T>` api.
It can sample WEKA .arff files and turn them into infinite data streams where you can precisely control the evolution of
probability distributions of the classes for both gradual and abrupt changes.

### Why?

Change detection algorithms need data to test upon. There are not many such datasets. The motivating hypothesis is that 
we will be able to provide a better challenge by using the separability of existing classification datasets 
rather than inducing artificial change arbitrarily.

### Concepts

Meander uses the change generation framework discussed in [2] and [3].

We can say that a data source ![S_i, i=1,...,K](https://latex.codecogs.com/gif.latex?S_i%2C%20i%3D1%2C...%2CK) is a set of class conditional probability density functions and prior probabilities.

![p_i(\vec{x}|y_j)](https://latex.codecogs.com/gif.latex?p_i%28%5Cvec%7Bx%7D%7Cy_j%29)

![p_i(y_j)$, $j = 1, ..., k](https://latex.codecogs.com/gif.latex?p_i%28y_j%29%24%2C%20%24j%20%3D%201%2C%20...%2C%20k)

A changing data stream at time `t` can be considered a mixture of these prior probabilities and class conditional probability density functions.


This is controlled by a mixing function 

![\upsilon_i(t) \in [0,1]](https://latex.codecogs.com/gif.latex?%5Cupsilon_i%28t%29%20%5Cin%20%5B0%2C1%5D)

![\sum_{i=1}^{K} \upsilon_i(t) = 1](https://latex.codecogs.com/gif.latex?%5Csum_%7Bi%3D1%7D%5E%7BK%7D%20%5Cupsilon_i%28t%29%20%3D%201)

for any `t`.

### Usage

Here, we build a stream from an `.arff` dataset with three classes. We start with a distribution of `[0 0.5 0.5]` over 
these classes, change gradually to `[1.0 0.0 .0.0]` over indices `20..33`, change abruptly to `[0.0, 0.0, 1.0]` at index `50`,
 and change gradually to `[0.33... 0.33... 0.33...]` over indices `60..70`.

```
Stream<Example> arffStream = ChangeStreamBuilder
        .fromArff(new InputStreamReader(ArffStreamTests.class.getResourceAsStream("abalone.arff")))
        .withPriors(0, 0.5, 0.5).fromStart()
        .withPriors(1.0, 0.0, 0.0).transition(new LinearTransition(20,33))
        .withPriors(0.0, 0.0, 1.0).transition(new AbruptTransition(50))
        .withUniformPriors().transition(new LinearTransition(60,70))
        .build();

arffStream.limit(100).forEach(x -> {
    StreamContext ctx = x.getContext();
    System.out.println(String.format(
            "i=%-5d|S=%-2d|ω=%-2d| %-35s| %-35s| %-35s| %s",
            ctx.getIndex(),
            ctx.getSequence(),
            ctx.getLabel(),
            ctx.getCurrentTransition().isPresent() ? ctx.getCurrentTransition().get() : "",
            doubleArrayToString(ctx.getSourcePriors(), "%.2f"),
            doubleArrayToString(ctx.getClassPriors(), "%.2f"),
            Arrays.toString(x.getData())));
});
```

100 examples are then drawn and printed from the stream as follows.

```
index  |src |cls |transition                          | source priors                      | joint class priors                 | data
i=0    |S=0 |ω=2 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [-1.15421, 0.383103, 0.273265, 0.728801, 0.09025, 0.101965, 0.021953, 0.227504]
i=1    |S=0 |ω=1 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [-1.15421, 0.591275, 0.777094, 0.250642, 0.519502, 0.770996, 0.459853, 0.267015]
i=2    |S=0 |ω=1 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [1.26179, -0.074876, -0.230565, -0.347058, -0.443, -0.188624, -0.493491, -0.63814]
i=3    |S=0 |ω=2 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [-1.15421, 1.04925, 1.02901, 0.489721, 1.13126, 1.25756, 1.00267, 0.917146]
i=4    |S=0 |ω=1 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [-1.15421, 0.882716, 0.726711, 0.609261, 0.932439, 1.10213, 0.824769, 0.798614]
i=5    |S=0 |ω=1 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [-1.15421, 0.508006, 0.424414, 0.967881, -0.059631, -0.105277, 0.044761, -0.013152]
i=6    |S=0 |ω=2 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [-1.15421, 1.09089, 0.827477, 0.848341, 1.08028, 1.26207, 1.0346, 0.90637]
i=7    |S=0 |ω=2 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [0.053792, -1.15737, -1.03669, -0.347058, -1.04966, -0.961275, -1.14122, -1.141]
i=8    |S=0 |ω=2 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [0.053792, -0.158145, -0.280948, -0.107978, -0.292099, 0.007355, -0.219804, -0.559119]
i=9    |S=0 |ω=2 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [0.053792, -1.19901, -0.835161, -0.705677, -1.0568, -1.07841, -0.972443, -0.96141]
i=10   |S=0 |ω=2 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [1.26179, 0.299834, 0.172499, 0.131102, -0.287001, -0.224666, -0.456999, -0.16042]
i=11   |S=0 |ω=2 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [0.053792, 1.04925, 0.978626, 0.848341, 0.772362, 0.680891, 0.742662, 1.08596]
i=12   |S=0 |ω=2 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [1.26179, -0.49122, -0.835161, 0.011562, -0.13712, 0.01186, -0.247172, -0.214298]
i=13   |S=0 |ω=2 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [-1.15421, 0.674544, 0.474797, 0.489721, 0.596991, 0.782259, 0.843014, 0.231096]
i=14   |S=0 |ω=2 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [-1.15421, -1.49045, -1.54052, -1.18384, -1.28111, -1.19555, -1.37841, -1.26313]
i=15   |S=0 |ω=1 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [1.26179, -2.03169, -1.8932, -1.42292, -1.42181, -1.3217, -1.42859, -1.50019]
i=16   |S=0 |ω=2 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [-1.15421, 0.091662, 0.323648, 0.609261, 0.094329, -0.362076, -0.005415, 0.439425]
i=17   |S=0 |ω=1 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [-1.15421, 0.508006, 0.474797, 0.250642, 0.157544, 0.22586, 0.45073, -0.038296]
i=18   |S=0 |ω=2 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [1.26179, -0.033242, 0.02135, 0.131102, -0.372647, -0.283234, -0.192435, -0.530384]
i=19   |S=0 |ω=1 |                                    | [1.00,0.00,0.00,0.00]              | [0.00,0.50,0.50]                   | [1.26179, -2.03169, -2.90086, -1.54246, -1.45138, -1.41405, -1.38297, -1.46787]
i=20   |S=0 |ω=1 | LinearTransition [start=20, end=33]| [0.93,0.07,0.00,0.00]              | [0.07,0.47,0.47]                   | [-1.15421, -2.07333, -2.09474, -1.42292, -1.48809, -1.45235, -1.42859, -1.50019]
i=21   |S=0 |ω=2 | LinearTransition [start=20, end=33]| [0.87,0.13,0.00,0.00]              | [0.13,0.43,0.43]                   | [0.053792, 0.757812, 1.12978, 0.250642, 1.04969, 1.21476, 0.728978, 0.762695]
i=22   |S=1 |ω=0 | LinearTransition [start=20, end=33]| [0.80,0.20,0.00,0.00]              | [0.20,0.40,0.40]                   | [-1.15421, 1.04925, 1.02901, 0.848341, 1.25871, 1.30262, 0.820207, 1.51699]
i=23   |S=0 |ω=2 | LinearTransition [start=20, end=33]| [0.73,0.27,0.00,0.00]              | [0.27,0.37,0.37]                   | [-1.15421, -0.074876, -0.230565, -0.227518, -0.419549, -0.332792, -0.475245, -0.458546]
i=24   |S=0 |ω=1 | LinearTransition [start=20, end=33]| [0.67,0.33,0.00,0.00]              | [0.33,0.33,0.33]                   | [1.26179, 0.216565, 0.273265, 0.609261, -0.050454, -0.087255, 0.122305, 0.037134]
i=25   |S=0 |ω=2 | LinearTransition [start=20, end=33]| [0.60,0.40,0.00,0.00]              | [0.40,0.30,0.30]                   | [0.053792, 1.17416, 1.18016, 1.68512, 1.66247, 2.06626, 1.44969, 1.35536]
i=26   |S=0 |ω=1 | LinearTransition [start=20, end=33]| [0.53,0.47,0.00,0.00]              | [0.47,0.27,0.27]                   | [1.26179, -2.03169, -2.04435, -1.662, -1.48299, -1.43433, -1.37385, -1.50019]
i=27   |S=1 |ω=0 | LinearTransition [start=20, end=33]| [0.47,0.53,0.00,0.00]              | [0.53,0.23,0.23]                   | [0.053792, 0.466371, 0.575563, 0.131102, 0.321699, 0.498428, 0.318448, 0.080237]
i=28   |S=1 |ω=0 | LinearTransition [start=20, end=33]| [0.40,0.60,0.00,0.00]              | [0.60,0.20,0.20]                   | [-1.15421, 0.299834, 0.02135, 0.609261, 0.206485, -0.03995, 0.51459, 0.439425]
i=29   |S=0 |ω=1 | LinearTransition [start=20, end=33]| [0.33,0.67,0.00,0.00]              | [0.67,0.17,0.17]                   | [-1.15421, 0.54964, 0.625945, 0.250642, 0.340052, 0.547986, 0.468975, 0.008399]
i=30   |S=1 |ω=0 | LinearTransition [start=20, end=33]| [0.27,0.73,0.00,0.00]              | [0.73,0.13,0.13]                   | [-1.15421, 0.841081, 0.827477, 0.609261, 0.723421, 0.710175, 0.519151, 1.08596]
i=31   |S=1 |ω=0 | LinearTransition [start=20, end=33]| [0.20,0.80,0.00,0.00]              | [0.80,0.10,0.10]                   | [1.26179, -0.366317, -0.180182, 0.131102, -0.502136, -0.576076, -0.543667, 0.116155]
i=32   |S=0 |ω=2 | LinearTransition [start=20, end=33]| [0.13,0.87,0.00,0.00]              | [0.87,0.07,0.07]                   | [-1.15421, 1.42396, 1.53284, 1.08742, 1.85823, 2.36811, 1.74162, 1.42719]
i=33   |S=1 |ω=0 | LinearTransition [start=20, end=33]| [0.07,0.93,0.00,0.00]              | [0.93,0.03,0.03]                   | [-1.15421, -0.158145, -0.280948, -0.227518, -0.352255, -0.596349, -0.0191, -0.135277]
i=34   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [1.26179, 0.799447, 0.827477, 0.489721, 0.483816, 0.390302, 0.596696, 0.475344]
i=35   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [0.053792, -0.033242, -0.129799, 0.967881, -0.384883, -0.907212, -0.644019, 0.044318]
i=36   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [-1.15421, 0.508006, 0.424414, 0.250642, 0.445071, 0.324975, 0.870383, 0.547182]
i=37   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [-1.15421, 0.54964, 0.52518, -0.227518, 0.557227, 0.430849, 0.227219, 0.654938]
i=38   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [1.26179, -0.616124, -0.583246, 0.011562, -0.723389, -0.672939, -0.653141, -0.799775]
i=39   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [-1.15421, 1.29906, 1.28092, 1.08742, 1.58702, 1.64502, 1.28548, 1.86181]
i=40   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [-1.15421, 0.216565, 0.02135, -0.347058, -0.139159, -0.490476, -0.155943, -0.314871]
i=41   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [-1.15421, 0.341468, 0.474797, 0.728801, 0.158564, -0.078245, -0.224365, 0.5831]
i=42   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [-1.15421, 1.00762, 0.87786, 1.20696, 1.44836, 1.13592, 2.07461, 1.48107]
i=43   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [-1.15421, -0.199779, -0.280948, 0.370181, -0.344099, -0.422897, -0.402262, -0.171195]
i=44   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [0.053792, 0.216565, 0.323648, 0.370181, 0.239112, -0.208897, 0.017392, 0.690857]
i=45   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [0.053792, 0.466371, 0.676328, 0.609261, 0.427738, 0.246133, 0.724417, 0.475344]
i=46   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [1.26179, 1.13252, 1.18016, 1.80466, 1.95204, 1.41074, 1.65039, 1.2045]
i=47   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [0.053792, 1.17416, 1.07939, 1.08742, 1.04358, 0.901648, 1.02547, 1.42719]
i=48   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [0.053792, 1.84031, 1.78475, 1.20696, 2.31909, 2.18114, 2.33917, 2.49039]
i=49   |S=1 |ω=0 |                                    | [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [0.053792, 1.00762, 1.02901, 1.44604, 1.50647, 1.17872, 1.70057, 1.58883]
i=50   |S=1 |ω=0 | AbruptTransition [start=50, end=50]| [0.00,1.00,0.00,0.00]              | [1.00,0.00,0.00]                   | [0.053792, -0.324683, -0.432097, 0.011562, -0.426686, -0.449929, -0.329278, -0.444179]
i=51   |S=2 |ω=2 |                                    | [0.00,0.00,1.00,0.00]              | [0.00,0.00,1.00]                   | [0.053792, 1.50723, 1.23054, 0.728801, 1.49934, 1.13817, 1.94689, 1.44515]
i=52   |S=2 |ω=2 |                                    | [0.00,0.00,1.00,0.00]              | [0.00,0.00,1.00]                   | [1.26179, -0.324683, -0.180182, -0.347058, -0.484803, -0.326034, -0.361209, -0.853654]
i=53   |S=2 |ω=2 |                                    | [0.00,0.00,1.00,0.00]              | [0.00,0.00,1.00]                   | [1.26179, 0.133296, -0.129799, -0.107978, -0.353275, -0.400371, -0.233488, -0.336422]
i=54   |S=2 |ω=2 |                                    | [0.00,0.00,1.00,0.00]              | [0.00,0.00,1.00]                   | [-1.15421, 0.674544, 0.676328, 0.848341, 0.864126, 0.892638, 1.13951, 0.748327]
i=55   |S=2 |ω=2 |                                    | [0.00,0.00,1.00,0.00]              | [0.00,0.00,1.00]                   | [0.053792, -0.158145, -0.331331, -0.586137, -0.487862, -0.431908, -0.55279, -0.516016]
i=56   |S=2 |ω=2 |                                    | [0.00,0.00,1.00,0.00]              | [0.00,0.00,1.00]                   | [0.053792, 0.216565, -0.280948, 0.609261, 0.767264, 0.827312, 1.0346, 0.687265]
i=57   |S=2 |ω=2 |                                    | [0.00,0.00,1.00,0.00]              | [0.00,0.00,1.00]                   | [-1.15421, 0.383103, 0.323648, 0.848341, 0.229935, 0.095207, 0.437045, 0.317301]
i=58   |S=2 |ω=2 |                                    | [0.00,0.00,1.00,0.00]              | [0.00,0.00,1.00]                   | [0.053792, 1.54887, 1.33131, 1.56558, 1.53298, 1.99417, 0.970735, 1.40923]
i=59   |S=2 |ω=2 |                                    | [0.00,0.00,1.00,0.00]              | [0.00,0.00,1.00]                   | [0.053792, 1.17416, 1.23054, 0.967881, 1.35047, 1.23954, 1.47249, 1.40923]
i=60   |S=2 |ω=2 | LinearTransition [start=60, end=70]| [0.00,0.00,0.92,0.08]              | [0.03,0.03,0.94]                   | [-1.15421, 0.674544, 0.676328, 0.848341, 0.864126, 0.892638, 1.13951, 0.748327]
i=61   |S=2 |ω=2 | LinearTransition [start=60, end=70]| [0.00,0.00,0.83,0.17]              | [0.06,0.06,0.89]                   | [1.26179, 1.04925, 1.12978, 0.250642, 0.834558, 0.856596, 1.05284, 0.65853]
i=62   |S=2 |ω=2 | LinearTransition [start=60, end=70]| [0.00,0.00,0.75,0.25]              | [0.08,0.08,0.83]                   | [0.053792, 0.424737, 0.222882, 0.370181, -0.067787, -0.048961, 0.108621, -0.135277]
i=63   |S=3 |ω=2 | LinearTransition [start=60, end=70]| [0.00,0.00,0.67,0.33]              | [0.11,0.11,0.78]                   | [-1.15421, 0.424737, 0.374031, 0.131102, 0.096368, 0.090702, -0.174189, 0.223912]
i=64   |S=3 |ω=2 | LinearTransition [start=60, end=70]| [0.00,0.00,0.58,0.42]              | [0.14,0.14,0.72]                   | [0.053792, 0.17493, 0.273265, 0.250642, -0.292099, -0.310266, -0.324717, -0.099358]
i=65   |S=3 |ω=2 | LinearTransition [start=60, end=70]| [0.00,0.00,0.50,0.50]              | [0.17,0.17,0.67]                   | [-1.15421, 0.424737, 0.625945, 1.08742, 0.318641, 0.068176, 0.336693, 0.834532]
i=66   |S=3 |ω=1 | LinearTransition [start=60, end=70]| [0.00,0.00,0.42,0.58]              | [0.19,0.19,0.61]                   | [1.26179, -1.11574, -1.18784, -1.18384, -1.13021, -1.01083, -1.23245, -1.15537]
i=67   |S=2 |ω=2 | LinearTransition [start=60, end=70]| [0.00,0.00,0.33,0.67]              | [0.22,0.22,0.56]                   | [0.053792, -0.574489, -0.583246, 0.011562, -0.522528, -0.73376, -0.438753, -0.458546]
i=68   |S=3 |ω=1 | LinearTransition [start=60, end=70]| [0.00,0.00,0.25,0.75]              | [0.25,0.25,0.50]                   | [1.26179, -0.741027, -0.784778, -0.705677, -0.84778, -0.693212, -0.771739, -1.02606]
i=69   |S=3 |ω=0 | LinearTransition [start=60, end=70]| [0.00,0.00,0.17,0.83]              | [0.28,0.28,0.44]                   | [1.26179, 1.13252, 1.18016, 0.967881, 1.76341, 1.09537, 2.00618, 1.30148]
i=70   |S=3 |ω=0 | LinearTransition [start=60, end=70]| [0.00,0.00,0.08,0.92]              | [0.31,0.31,0.39]                   | [-1.15421, -0.907565, -0.885544, -0.944757, -1.07719, -1.07165, -1.12297, -0.968594]
i=71   |S=3 |ω=2 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [0.053792, -0.699393, -0.583246, -0.347058, -0.867153, -0.830623, -1.07279, -0.789]
i=72   |S=3 |ω=1 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [1.26179, -0.616124, -0.784778, -0.825217, -0.932407, -0.778812, -0.93139, -1.01529]
i=73   |S=3 |ω=2 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [0.053792, 0.008393, 0.071733, 0.250642, -0.251315, -0.136813, -0.306471, -0.286136]
i=74   |S=3 |ω=2 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [1.26179, -0.033242, -0.129799, -0.107978, -0.399157, -0.274224, -0.466122, -0.458546]
i=75   |S=3 |ω=2 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [1.26179, -0.074876, -0.180182, -0.466597, -0.537822, -0.560307, -0.415946, -0.429811]
i=76   |S=3 |ω=0 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [1.26179, 0.799447, 0.777094, 0.967881, 0.663265, 0.604301, 0.687925, 0.547182]
i=77   |S=3 |ω=2 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [1.26179, 0.133296, 0.172499, -0.227518, -0.220727, -0.287739, -0.105767, -0.099358]
i=78   |S=3 |ω=1 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [-1.15421, 0.466371, 0.424414, 0.131102, 0.354327, 0.845333, 0.154235, -0.067031]
i=79   |S=3 |ω=2 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [-1.15421, 0.466371, 0.625945, 0.609261, 0.344131, 0.153776, 0.560204, 0.654938]
i=80   |S=3 |ω=2 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [0.053792, 0.050027, -0.230565, -0.347058, -0.324726, -0.317024, -0.26998, -0.422627]
i=81   |S=3 |ω=2 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [-1.15421, 0.383103, 0.273265, 0.131102, 0.156524, 0.14927, 0.500906, 0.259831]
i=82   |S=3 |ω=0 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [1.26179, 0.258199, 0.474797, 0.728801, 0.030094, -0.226918, 0.090375, 0.439425]
i=83   |S=3 |ω=1 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [1.26179, -2.03169, -1.99397, -1.78154, -1.46362, -1.42757, -1.38753, -1.50019]
i=84   |S=3 |ω=1 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [1.26179, -1.0741, -1.08708, -1.18384, -1.12103, -1.01534, -1.20052, -1.17692]
i=85   |S=3 |ω=0 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [0.053792, 1.42396, 1.28092, 1.44604, 1.54318, 1.38596, 1.39951, 1.66785]
i=86   |S=3 |ω=0 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [0.053792, 0.341468, 0.978626, 1.68512, 0.913067, 0.638091, 0.897752, 0.834532]
i=87   |S=3 |ω=0 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [1.26179, 0.632909, 0.374031, 0.848341, 0.465463, 0.106471, 0.323009, 0.834532]
i=88   |S=3 |ω=0 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [0.053792, 0.591275, 0.625945, 2.64144, 0.926321, 0.462386, 0.61038, 1.48107]
i=89   |S=3 |ω=2 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [-1.15421, 1.38233, 0.978626, 1.44604, 2.12741, 2.40415, 2.01531, 1.7325]
i=90   |S=3 |ω=2 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [1.26179, -0.574489, -0.381714, -0.347058, -0.806996, -0.713486, -0.493491, -0.674059]
i=91   |S=3 |ω=0 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [1.26179, -0.782662, -0.835161, -0.705677, -0.940564, -1.00858, -0.794546, -0.853654]
i=92   |S=3 |ω=1 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [1.26179, -3.11419, -3.10239, -2.73785, -1.65938, -1.59877, -1.61105, -1.67979]
i=93   |S=3 |ω=0 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [-1.15421, -0.366317, -0.280948, -0.107978, -0.613273, -0.717991, -0.374893, -0.566303]
i=94   |S=3 |ω=1 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [0.053792, -0.824296, -0.583246, -0.944757, -0.787624, -0.830623, -0.958759, -0.458546]
i=95   |S=3 |ω=2 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [-1.15421, 0.424737, 0.424414, -0.227518, -0.089199, -0.186371, 0.113182, -0.088582]
i=96   |S=3 |ω=2 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [0.053792, 0.424737, 0.625945, 0.370181, 0.585775, 0.674133, 0.523713, 0.726776]
i=97   |S=3 |ω=0 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [0.053792, 0.92435, 1.12978, 0.609261, 1.04358, 0.66287, 1.05284, 1.24401]
i=98   |S=3 |ω=0 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [0.053792, 0.466371, 0.474797, -0.466597, 0.226877, 0.178555, 0.697048, 0.187993]
i=99   |S=3 |ω=2 |                                    | [0.00,0.00,0.00,1.00]              | [0.33,0.33,0.33]                   | [-1.15421, 0.466371, 0.474797, -0.107978, -0.067787, 0.205586, -0.124013, -0.250217]
```

### References

[1] [Kuncheva, Ludmila I. "Change detection in streaming multivariate data using likelihood detectors." IEEE Transactions on Knowledge and Data Engineering 25.5 (2013): 1175-1180.](http://ieeexplore.ieee.org/abstract/document/6060824/)

[2] [Narasimhamurthy, Anand M., and Ludmila I. Kuncheva. "A framework for generating data to simulate changing environments." Artificial Intelligence and Applications. 2007.](http://pages.bangor.ac.uk/~mas00a/papers/anlkAIA07.pdf)

[3] [Bifet, Albert, et al. "New ensemble methods for evolving data streams." Proceedings of the 15th ACM SIGKDD international conference on Knowledge discovery and data mining. ACM, 2009.](https://researchcommons.waikato.ac.nz/bitstream/handle/10289/3982/New%20Ensemble%20methods.pdf?sequence=1&isAllowed=y)
