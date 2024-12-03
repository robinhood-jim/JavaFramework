package com.robin.dataming.weka.algorithm;

import org.springframework.util.ObjectUtils;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.GaussianProcesses;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Map;

public class TimeSeriesModeler extends AbstractModeler<GaussianProcesses> {

    // GaussianProcesses 分类器在 Weka 中的 setOptions 方法支持多种选项，这些选项用于配置分类器的内部参数。以下是一些常用的选项和它们的含义：
    // -L <double>: 高斯噪声水平相对于转换目标的设置。默认值为1。
    // -N: 是否进行标准化/规范化/不进行任何处理。默认值为0，表示进行标准化。
    // -K <classname and parameters>: 用于指定核函数的选项。默认使用 weka.classifiers.functions.supportVector.PolyKernel 多项式核函数。
    // -S <num>: 随机数种子，用于初始化算法的随机性。默认值为1。
    // -output-debug-info: 如果设置，分类器将以调试模式运行，并可能在控制台上输出额外的信息。
    // -do-not-check-capabilities: 如果设置，将在构建分类器之前不检查分类器的能力（请小心使用）。
    // -num-decimal-places: 模型中数字输出的小数位数。默认值为2。
    //  以下是对多项式核函数的特定选项：weka.classifiers.functions.supportVector.PolyKernel
    //      -E <num>: 多项式核的指数。默认值为1.0。
    //      -L: 是否使用低阶项。默认值为否（no）。
    //      -C <num>: 缓存的大小（质数），0表示完整缓存，-1表示关闭缓存。默认值为250007。
    //      -output-debug-info: 启用调试输出（如果可用）。默认值为关闭。
    //      -no-checks:关闭所有检查 - 谨慎使用。默认值为开启检查。

    @Override
    public GaussianProcesses train(int classIndex, Map<String, String> optionMap, Instances trainInst) throws Exception {
        trainInst.setClassIndex(classIndex);
        if(!ObjectUtils.isEmpty(optionMap.get("randomSeed"))){
            model.setSeed(Integer.parseInt(optionMap.get("randomSeed").toString()));
        }
        setOptions(optionMap);
        model.buildClassifier(trainInst);
        return model;
    }



}
