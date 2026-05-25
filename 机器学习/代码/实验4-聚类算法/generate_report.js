const fs = require('fs');
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, ImageRun,
  Header, Footer, AlignmentType, HeadingLevel, BorderStyle, WidthType,
  ShadingType, PageNumber, PageBreak, LevelFormat
} = require('docx');

const OUT = '../../输出结果/实验4-clustering_outputs/';

// Content width for A4: 11906 - 2*1800 = 8306 DXA
const CW = 8306;
const CW_HALF = 4153;

// Common borders
const cellBorder = { style: BorderStyle.SINGLE, size: 4, color: '000000' };
const borders = { top: cellBorder, bottom: cellBorder, left: cellBorder, right: cellBorder };
const noBorders = {
  top: { style: BorderStyle.NONE, size: 0 },
  bottom: { style: BorderStyle.NONE, size: 0 },
  left: { style: BorderStyle.NONE, size: 0 },
  right: { style: BorderStyle.NONE, size: 0 },
};
const bottomBorder = {
  top: { style: BorderStyle.NONE, size: 0 },
  bottom: { style: BorderStyle.SINGLE, size: 4, color: '000000' },
  left: { style: BorderStyle.NONE, size: 0 },
  right: { style: BorderStyle.NONE, size: 0 },
};

function p(text, options = {}) {
  const runs = [];
  if (typeof text === 'string') {
    runs.push(new TextRun({ text, ...options }));
  } else if (Array.isArray(text)) {
    text.forEach(t => {
      if (typeof t === 'string') runs.push(new TextRun({ text: t, ...options }));
      else runs.push(new TextRun({ ...options, ...t }));
    });
  }
  return new Paragraph({
    spacing: { line: 360 },
    children: runs,
  });
}

function sectionTitle(text) {
  return new Paragraph({
    spacing: { before: 240, after: 120, line: 360 },
    children: [new TextRun({ text, bold: true, size: 24, font: '黑体' })],
  });
}

function subTitle(text) {
  return new Paragraph({
    spacing: { before: 160, after: 80, line: 360 },
    children: [new TextRun({ text, bold: true, size: 24, font: '宋体' })],
  });
}

function bodyText(text, indent = false) {
  return new Paragraph({
    spacing: { line: 360 },
    indent: indent ? { firstLine: 480 } : undefined,
    children: [new TextRun({ text, size: 24, font: '宋体' })],
  });
}

function tableCell(text, opts = {}) {
  const { width, bold, fill, align, gridSpan, margins } = opts;
  const children = [];
  if (typeof text === 'string') {
    children.push(new Paragraph({
      spacing: { line: 360 },
      alignment: align || AlignmentType.LEFT,
      children: [new TextRun({ text, size: 24, font: '宋体', bold: !!bold })],
    }));
  } else if (Array.isArray(text)) {
    text.forEach(t => {
      children.push(new Paragraph({
        spacing: { line: 360 },
        alignment: align || AlignmentType.LEFT,
        children: [new TextRun({ text: t, size: 24, font: '宋体', bold: !!bold })],
      }));
    });
  }
  const cellOpts = {
    borders,
    width: width ? { size: width, type: WidthType.DXA } : undefined,
    margins: margins || { top: 60, bottom: 60, left: 120, right: 120 },
    children,
  };
  if (fill) cellOpts.shading = { fill, type: ShadingType.CLEAR };
  if (gridSpan) cellOpts.columnSpan = gridSpan;
  return new TableCell(cellOpts);
}

function imagePara(imgPath, widthPx, heightPx) {
  return new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 120, after: 120 },
    children: [new ImageRun({
      type: 'png',
      data: fs.readFileSync(imgPath),
      transformation: { width: widthPx, height: heightPx },
      altText: { title: 'Figure', description: 'Experiment figure', name: 'Figure' },
    })],
  });
}

function caption(text) {
  return new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 200, line: 360 },
    children: [new TextRun({ text, size: 20, font: '宋体', italics: true, color: '555555' })],
  });
}

// ====================== BUILD DOCUMENT ======================

const doc = new Document({
  styles: {
    default: {
      document: { run: { font: '宋体', size: 24 } },
    },
  },
  sections: [{
    properties: {
      page: {
        size: { width: 11906, height: 16838 },
        margin: { top: 1440, right: 1800, bottom: 1440, left: 1800 },
      },
    },
    headers: {
      default: new Header({
        children: [new Paragraph({
          alignment: AlignmentType.CENTER,
          children: [new TextRun({ text: '《机器学习基础》实验四：聚类算法实践', size: 18, font: '宋体', color: '888888' })],
        })],
      }),
    },
    footers: {
      default: new Footer({
        children: [new Paragraph({
          alignment: AlignmentType.CENTER,
          children: [
            new TextRun({ text: '第 ', size: 18, font: '宋体' }),
            new TextRun({ children: [PageNumber.CURRENT], size: 18, font: '宋体' }),
            new TextRun({ text: ' 页', size: 18, font: '宋体' }),
          ],
        })],
      }),
    },
    children: [

      // ===== TITLE =====
      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { after: 400 },
        children: [new TextRun({ text: '《机器学习基础》实验报告', bold: true, size: 32, font: '黑体' })],
      }),

      // ===== INFO TABLE =====
      new Table({
        width: { size: CW, type: WidthType.DXA },
        columnWidths: [1263, 2029, 1365, 3649],
        rows: [
          // Row 1
          new TableRow({
            children: [
              tableCell('年级、专业、班级', { width: 1263, bold: true, align: AlignmentType.CENTER }),
              tableCell('2024级', { width: 2029, bold: true }),
              tableCell('姓名', { width: 1365, bold: true, align: AlignmentType.CENTER }),
              tableCell('', { width: 3649 }),
            ],
          }),
          // Row 2
          new TableRow({
            children: [
              tableCell('实验题目', { width: 1263, bold: true, align: AlignmentType.CENTER }),
              tableCell('聚类算法实践', { width: 7043, bold: true, align: AlignmentType.CENTER, gridSpan: 3 }),
            ],
          }),
          // Row 3
          new TableRow({
            children: [
              tableCell('实验时间', { width: 1263, bold: true, align: AlignmentType.CENTER }),
              tableCell('2026年5月24日', { width: 2029, bold: true }),
              tableCell('实验地点', { width: 1365, bold: true, align: AlignmentType.CENTER }),
              tableCell('DS1402', { width: 3649, bold: true, align: AlignmentType.CENTER }),
            ],
          }),
          // Row 4
          new TableRow({
            children: [
              tableCell('实验成绩', { width: 1263, bold: true, align: AlignmentType.CENTER }),
              tableCell('', { width: 2029 }),
              tableCell('实验性质', { width: 1365, bold: true, align: AlignmentType.CENTER }),
              tableCell('☑ 设计性  □ 验证性  □ 综合性', { width: 3649 }),
            ],
          }),
        ],
      }),

      // ===== Teacher evaluation =====
      new Table({
        width: { size: CW, type: WidthType.DXA },
        columnWidths: [CW],
        rows: [
          new TableRow({
            children: [
              new TableCell({
                borders: bottomBorder,
                margins: { top: 60, bottom: 60, left: 120, right: 120 },
                width: { size: CW, type: WidthType.DXA },
                children: [
                  new Paragraph({
                    spacing: { line: 360 },
                    children: [new TextRun({ text: '教师评价：', bold: true, size: 24, font: '黑体' })],
                  }),
                  new Paragraph({
                    spacing: { line: 360 },
                    children: [new TextRun({ text: '□ 算法/实验过程正确；  □ 源程序/实验内容提交    □ 程序结构/实验步骤合理；', size: 24, font: '楷体_GB2312' })],
                  }),
                  new Paragraph({
                    spacing: { line: 360 },
                    children: [new TextRun({ text: '□ 实验结果正确；       □ 语法、语义正确；      □ 报告规范；', size: 24, font: '楷体_GB2312' })],
                  }),
                  new Paragraph({
                    spacing: { line: 360 },
                    children: [new TextRun({ text: '其他：', size: 24, font: '楷体_GB2312' })],
                  }),
                  new Paragraph({
                    spacing: { line: 360 },
                    children: [new TextRun({ text: '                                          评价教师签名： ', size: 24, font: '宋体' })],
                  }),
                ],
              }),
            ],
          }),
        ],
      }),

      // ===== SECTION 1: 实验目的 =====
      sectionTitle('一、实验目的'),
      bodyText('1. 理解并掌握原型聚类算法（K-means）和密度聚类算法（DBSCAN）的基本原理。', true),
      bodyText('2. 编程实现K-means聚类算法，掌握聚类算法的应用方法。', true),
      bodyText('3. 通过不同K值和不同初始中心点的对比实验，分析影响聚类结果的关键因素，学习聚类性能评价方法。', true),

      // ===== SECTION 2: 实验项目内容 =====
      sectionTitle('二、实验项目内容'),
      bodyText('1. 理解并描述 K-means（原型聚类算法）和 DBSCAN（密度聚类算法）的原理。', true),
      bodyText('2. 编程实践：将K-means算法应用于Iris鸢尾花数据集和西瓜数据集4.0，设置4组不同的K值（K=2,3,4,5），每组K值使用3组不同随机初始中心点，对比实验结果，使用Silhouette Score、SSE、Davies-Bouldin Index等指标分析聚类结果的优劣。输出聚类结果图和决策边界图。', true),

      // ===== SECTION 3: 实验过程或算法 =====
      sectionTitle('三、实验过程或算法（源程序）'),

      subTitle('3.1 K-means算法原理（原型聚类）'),
      bodyText('K-means算法是一种基于原型的聚类方法。其核心思想是：给定样本集D={x₁, x₂, ..., xₘ}，K-means算法将样本划分到K个簇中，使得每个样本到其所属簇中心（均值向量/质心）的距离平方和最小。', true),
      bodyText('算法步骤：', true),
      bodyText('（1）随机选择K个样本作为初始均值向量（初始中心点）；'),
      bodyText('（2）计算每个样本到K个均值向量的距离，将其划入距离最近的簇；'),
      bodyText('（3）重新计算每个簇的均值向量；'),
      bodyText('（4）重复步骤（2）和（3），直到均值向量不再变化或达到最大迭代次数。'),
      bodyText('目标函数（SSE）：E = Σᵢ Σ_{x∈Cᵢ} ||x - μᵢ||²，其中μᵢ是簇Cᵢ的均值向量。K-means通过迭代优化最小化SSE，但只能收敛到局部最优解，结果依赖于初始中心点的选择。', true),

      subTitle('3.2 DBSCAN算法原理（密度聚类）'),
      bodyText('DBSCAN（Density-Based Spatial Clustering of Applications with Noise）是一种基于密度的聚类算法。其核心思想是：通过样本分布的紧密程度来确定聚类结构，将簇定义为由密度可达关系导出的最大的密度相连样本集合。', true),
      bodyText('核心概念：', true),
      bodyText('（1）ε-邻域：以样本x为中心、ε为半径的区域；'),
      bodyText('（2）核心对象：ε-邻域内样本数≥MinPts的样本；'),
      bodyText('（3）密度直达：若xⱼ在xᵢ的ε-邻域内且xᵢ是核心对象，则xⱼ由xᵢ密度直达；'),
      bodyText('（4）密度可达：存在样本序列p₁, p₂, ..., pₙ，使pₖ₊₁由pₖ密度直达；'),
      bodyText('（5）密度相连：两样本由同一样本密度可达。'),
      bodyText('DBSCAN的优势在于：不需要预设簇数K，可以发现任意形状的簇，并能识别噪声点。', true),

      subTitle('3.3 实验设置与实现'),
      bodyText('数据集：Iris鸢尾花数据集（150个样本，4个特征，3个类别），西瓜数据集4.0（30个样本，2个特征）。对数据进行Z-score标准化处理，以消除特征尺度对K-means聚类的影响。', true),
      bodyText('K值设置：K ∈ {2, 3, 4, 5}（4组，超过要求的三组以上）。对每个K值，使用3组不同随机种子（random_state）进行初始化，共进行12组K-means实验。', true),
      bodyText('评价指标：SSE（簇内误差平方和）、Silhouette Score（轮廓系数）、Davies-Bouldin Index（DB指数）。', true),
      bodyText('实验环境：Python 3.12, scikit-learn 1.8.0, matplotlib 3.10.8, numpy 2.3.1。', true),

      subTitle('3.4 核心代码'),
      bodyText('以下为K-means聚类实验的核心代码（完整代码见附件 exp4_kmeans_clustering.py）：', true),
      new Paragraph({
        spacing: { before: 80, after: 80 },
        children: [
          new TextRun({
            text: `# K-means聚类核心代码
from sklearn.cluster import KMeans
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import silhouette_score, davies_bouldin_score

# 数据加载与标准化
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# 多K值、多初始化实验
K_VALUES = [2, 3, 4, 5]
N_INIT = 3
for k in K_VALUES:
    for init_idx in range(N_INIT):
        km = KMeans(n_clusters=k, random_state=init_idx*42+k*7,
                     n_init=1, init='random')
        y_pred = km.fit_predict(X_scaled)
        inertia = km.inertia_
        sil = silhouette_score(X_scaled, y_pred)
        db = davies_bouldin_score(X_scaled, y_pred)`,
            size: 20, font: 'Consolas', color: '333333',
          }),
        ],
      }),

      new Paragraph({ children: [new PageBreak()] }),

      // ===== SECTION 4: 实验结果及分析 =====
      sectionTitle('四、实验结果及分析'),

      subTitle('4.1 K-means聚类结果汇总'),

      // Results table
      new Table({
        width: { size: CW, type: WidthType.DXA },
        columnWidths: [800, 800, 1300, 1606, 1900, 1900],
        rows: [
          new TableRow({
            children: [
              tableCell('K值', { width: 800, bold: true, fill: 'D5E8F0', align: AlignmentType.CENTER }),
              tableCell('初始组', { width: 800, bold: true, fill: 'D5E8F0', align: AlignmentType.CENTER }),
              tableCell('SSE', { width: 1300, bold: true, fill: 'D5E8F0', align: AlignmentType.CENTER }),
              tableCell('Silhouette', { width: 1606, bold: true, fill: 'D5E8F0', align: AlignmentType.CENTER }),
              tableCell('Davies-Bouldin', { width: 1900, bold: true, fill: 'D5E8F0', align: AlignmentType.CENTER }),
              tableCell('聚类分布', { width: 1900, bold: true, fill: 'D5E8F0', align: AlignmentType.CENTER }),
            ],
          }),
          ...[
            ['2', '1', '222.362', '0.5818', '0.5933', '[50, 100]'],
            ['2', '2', '222.362', '0.5818', '0.5933', '[100, 50]'],
            ['2', '3', '222.362', '0.5818', '0.5933', '[100, 50]'],
            ['3', '1', '197.320', '0.4795', '0.6981', '[33, 17, 100]'],
            ['3', '2', '140.902', '0.4565', '0.8275', '[55, 46, 49]'],
            ['3', '3', '140.033', '0.4630', '0.8324', '[50, 44, 56]'],
            ['4', '1', '114.557', '0.4151', '0.9224', '[28, 49, 49, 24]'],
            ['4', '2', '114.557', '0.4151', '0.9224', '[24, 49, 49, 28]'],
            ['4', '3', '114.413', '0.4189', '0.9071', '[29, 22, 50, 49]'],
            ['5', '1', '91.047', '0.3414', '0.9518', '[45, 25, 25, 21, 34]'],
            ['5', '2', '90.808', '0.3455', '0.9452', '[29, 23, 48, 28, 22]'],
            ['5', '3', '105.933', '0.3596', '1.1368', '[34, 19, 49, 26, 22]'],
          ].map(row => new TableRow({
            children: row.map((cell, i) => tableCell(cell, {
              width: [800, 800, 1300, 1606, 1900, 1900][i],
              align: AlignmentType.CENTER,
            })),
          })),
        ],
      }),

      caption('表1：K-means聚类实验结果汇总（Iris数据集）'),

      bodyText('由表1可知：K=2时Silhouette Score最高(0.5818)，但这是由于将versicolor和virginica两类合并为一个簇，不符合实际3类结构；K=3时SSE下降明显（从222→140），且Silhouette保持在0.46水平，与真实3类吻合；K>3时Silhouette逐渐下降。', true),

      subTitle('4.2 可视化结果'),
      bodyText('以下图表展示了不同K值下的K-means聚类结果和决策边界：', true),

      // Figure 1: K-means results
      imagePara(OUT + 'fig1_kmeans_pca_results.png', 480, 490),
      caption('图1：Iris数据集K-means聚类结果（PCA降维到2D），红色X标记为质心'),

      // Figure 2: All inits comparison
      imagePara(OUT + 'fig2_kmeans_all_inits.png', 450, 504),
      caption('图2：各K值和初始中心点的聚类对比（每行：不同K值；第1列：真实分布；第2-4列：3组初始化）'),

      new Paragraph({ children: [new PageBreak()] }),

      // Figure 3: Decision boundary
      imagePara(OUT + 'fig3_decision_boundary.png', 460, 410),
      caption('图3：K-means决策边界图（基于前两个特征：Sepal Length和Sepal Width）'),

      bodyText('图3展示了K-means在特征空间中的Voronoi划分。K=2时将特征空间线性分割为两个区域；K=3时的划分边界与真实三类分布较为吻合，但Sepal Length和Sepal Width两个特征不足以完全区分versicolor和virginica；K=4和K=5对virginica区域进行了进一步细分。', true),

      // Figure 4: Performance metrics
      imagePara(OUT + 'fig4_performance_metrics.png', 500, 138),
      caption('图4：不同K值下的聚类性能指标对比'),

      subTitle('4.3 结果分析'),
      bodyText('（1）肘部法则分析：SSE随K增加而单调递减，在K=3处出现明显的"肘部"拐点，之后下降趋缓。这表明K=3是数据集的合理簇数，与Iris数据集的实际3个类别基本一致。', true),
      bodyText('（2）初始中心点敏感性：K=3时，三组不同初始化的SSE分别为197.32、140.90、140.03，标准差达26.80，其中第1组初始化陷入了较差的局部最优（setosa与部分versicolor被错误合并），说明K-means对初始中心点选择敏感。K=2时三组初始化结果一致（SSE标准差=0.00），因为对于K=2，Iris数据的簇结构较为明确。', true),
      bodyText('（3）最佳K值判定：综合考虑Silhouette Score（K=2最高但不符合实际）、SSE肘部拐点（K=3）和先验知识（3个物种），K=3是最合理的选择。', true),

      subTitle('4.4 DBSCAN密度聚类结果'),
      bodyText('为了与K-means对比，进行了DBSCAN密度聚类实验，参数设置为eps ∈ {0.5, 0.7, 0.9, 1.1}，min_samples=5。', true),

      // DBSCAN table
      new Table({
        width: { size: CW, type: WidthType.DXA },
        columnWidths: [1200, 1300, 906, 906, 1994, 2000],
        rows: [
          new TableRow({
            children: [
              tableCell('eps', { width: 1200, bold: true, fill: 'D5E8F0', align: AlignmentType.CENTER }),
              tableCell('min_samples', { width: 1300, bold: true, fill: 'D5E8F0', align: AlignmentType.CENTER }),
              tableCell('簇数', { width: 906, bold: true, fill: 'D5E8F0', align: AlignmentType.CENTER }),
              tableCell('噪声点', { width: 906, bold: true, fill: 'D5E8F0', align: AlignmentType.CENTER }),
              tableCell('Silhouette', { width: 1994, bold: true, fill: 'D5E8F0', align: AlignmentType.CENTER }),
              tableCell('Davies-Bouldin', { width: 2000, bold: true, fill: 'D5E8F0', align: AlignmentType.CENTER }),
            ],
          }),
          ...[
            ['0.5', '5', '2', '34', '0.6559', '0.4942'],
            ['0.7', '5', '2', '6', '0.6018', '0.5568'],
            ['0.9', '5', '2', '4', '0.5979', '0.5688'],
            ['1.1', '5', '2', '2', '0.5910', '0.5755'],
          ].map(row => new TableRow({
            children: row.map((cell, i) => tableCell(cell, {
              width: [1200, 1300, 906, 906, 1994, 2000][i],
              align: AlignmentType.CENTER,
            })),
          })),
        ],
      }),
      caption('表2：DBSCAN密度聚类实验结果'),

      bodyText('分析：DBSCAN在所有参数下均发现2个主簇，主要将setosa与其他两类区分开。eps=0.5时噪声点最多（34个），聚类过于严格；eps=0.7~0.9时效果较好（噪声点4-6个），Silhouette Score（0.60）高于K-means的K=3（0.46），说明从密度角度Iris数据更倾向于2个主要密度区域。DBSCAN的优势是不需要预设簇数，但参数(eps, min_samples)的选择直接影响聚类质量。', true),

      // DBSCAN figure
      imagePara(OUT + 'fig5_dbscan_results.png', 440, 450),
      caption('图5：DBSCAN不同参数的聚类结果（PCA降维），灰色X为噪声点'),

      new Paragraph({ children: [new PageBreak()] }),

      subTitle('4.5 西瓜数据集4.0补充实验'),
      bodyText('在西瓜数据集4.0（密度-含糖率二维特征，30个样本）上进行K-means聚类，验证算法的通用性。', true),

      imagePara(OUT + 'fig7_watermelon_kmeans.png', 440, 450),
      caption('图6：西瓜数据集4.0 K-means聚类结果及决策边界'),

      bodyText('在西瓜数据集上，K=3时Silhouette Score最高（0.540），可以将西瓜大致分为"低糖低密"、"中等"和"高糖高密"三类。该数据集样本量小（30个），聚类结果对初始中心点更为敏感。', true),

      subTitle('4.6 实验结论'),
      bodyText('（1）K-means算法能够有效地对Iris数据集进行聚类，K=3是最合理的簇数选择，与数据集的真实类别数一致。', true),
      bodyText('（2）K-means对初始中心点选择敏感，不同初始化可能导致不同的聚类结果和SSE值（差异可达40%），实际应用中建议多次运行取最优解。', true),
      bodyText('（3）DBSCAN不需要预设簇数，能够自动识别噪声点，在Iris数据上倾向于将数据划分为2个主要密度区域，聚类纯度更高但未能区分versicolor和virginica。', true),
      bodyText('（4）聚类评价需要综合多个指标（SSE肘部法则、Silhouette Score、DB Index）并结合领域知识，单一指标可能产生误导性结论。', true),
      bodyText('（5）数据标准化对K-means至关重要，因为K-means基于欧氏距离，不同特征的尺度差异会影响聚类结果。', true),

    ],
  }],
});

// Write the document
Packer.toBuffer(doc).then(buffer => {
  const outPath = '../../输出结果/实验4-clustering_outputs/实验4-聚类算法实践-实验报告.docx';
  fs.writeFileSync(outPath, buffer);
  console.log('Report generated: ' + outPath);
});
