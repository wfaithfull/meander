## Meander

##### Change Stream Generation for Java 8

Meander is a lightweight library for the generation of concept drifting data streams using the Java 8 `Stream<T>` api.
It can sample WEKA .arff files and turn them into infinite data streams where you can precisely control the evolution of
probability distributions of the classes for both gradual and abrupt changes.

##### Why?

Change detection algorithms need data to test upon. There are not many such datasets. The motivating hypothesis is that 
we will be able to provide a better challenge by using the separability of existing classification datasets 
rather than inducing artificial change arbitrarily.

##### Concepts

This library utilises the concepts from the following works to generate changing streams:

[Narasimhamurthy, Anand M., and Ludmila I. Kuncheva. "A framework for generating data to simulate changing environments." Artificial Intelligence and Applications. 2007.](http://pages.bangor.ac.uk/~mas00a/papers/anlkAIA07.pdf)

[Bifet, Albert, et al. "New ensemble methods for evolving data streams." Proceedings of the 15th ACM SIGKDD international conference on Knowledge discovery and data mining. ACM, 2009.](https://researchcommons.waikato.ac.nz/bitstream/handle/10289/3982/New%20Ensemble%20methods.pdf?sequence=1&isAllowed=y)

These papers describe change over time within a general framework. We can say that a data source ![S_i, i=1,...,K](http://www.sciweavers.org/upload/Tex2Img_1512415154/render.png) is a set of class conditional probability density functions ![](http://www.sciweavers.org/upload/Tex2Img_1512415255/render.png) and prior probabilities ![](http://www.sciweavers.org/upload/Tex2Img_1512415290/render.png).

A changing data stream at time `t` can be considered a mixture of these prior probabilities and class conditional probability density functions.

![](http://www.sciweavers.org/upload/Tex2Img_1512415402/render.png)

This is controlled by a mixing function ![](http://www.sciweavers.org/upload/Tex2Img_1512415449/render.png) where 

![](http://www.sciweavers.org/upload/Tex2Img_1512415480/render.png)

for any `t`.

##### Usage

Here, we build a stream from an `.arff` dataset with three classes. We start with a distribution of `[0 0.5 0.5]` over 
these classes, change abruptly to `[1.0 0.0 .0.0]` at `t=33` and change abruptly again to a uniform distribution `[0.33... 0.33... 0.33...]` at `t=66`.

```
Stream<Example> arffStream = ChangeStreamBuilder
    .fromArff(new InputStreamReader(ArffStreamTest.class.getResourceAsStream("abalone.arff")))
    .withClassMixture(0, 0.5, 0.5).fromStart()
    .withClassMixture(1.0, 0.0, 0.0).at(33)
    .withUniformClassMixture().at(66)
    .build();

arffStream.limit(100).forEach(x -> System.out.println(String.format(
    "i=%-5d|S=%-2d|ω=%-2d| %s",
    x.getContext().getIndex(),
    x.getContext().getSequence(),
    x.getContext().getLabel(),
    Arrays.toString(x.getData()))));
```

100 examples are then drawn and printed from the stream.