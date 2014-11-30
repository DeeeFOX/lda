package cn.scut.chiu.sampler;

/**
 * 
 * @author Chiu
 *
 */
public class GibbsSampler {

	
	// document data, term list a M*N matrix
	// docs[i][j] is No of the V table
	int[][] docs; 
	int V; //vocabulary size
	int K; //topic size
	int M; // docs size
	double alpha; // α参数,用于决定 doc--topic 分布 dirichlet parameter (doc--topic association)
	double beta; // β参数,用于决定topic--word分布 topic--word association
	double[][] thetasum; // θ分布，也就是α参数决定的doc--topic矩阵，cumulative statistics of θ
	double[][] phisum; // phi分布，也就是θ参数决定的topic--word矩阵，cumulative statistics of phi,什么的分布？
	// topic assignments for each word
	// M*N, 第一维是doc，第二维是word
	int[][] z;
	// nw[i][j] 是 word i 分配到 topic j 上次数的统计
	// V*K，第一维是word，第二维是topic
	int[][] nw;
	// nd[i][j] 是 doc i 中的词  分配到topic j 上次数的统计
	// M*K，第一维是doc，第二维是topic；也就是θ，符合dirichlet分布
	int[][] nd;
	// nwsum[j] total number of words assigned to topic j
	int[] nwsum;
	// ndsum[i] total number of words in doc i
	int[] ndsum;
	// size of statistics？？
	int numstats;
	// sampling lag？？
	private static int THIN_INTEVAL = 20;
	// burn in period
	// burn in次数以前的会被丢弃
	// burn in目的是使得sample开始前矩阵状态达到一定的稳定状态
	private static int BURN_IN = 100;
	// max iterations
	private static int ITERATIONS = 1000;
	// sample lag (if -1, only one sample taken)
	// sample间隔，用于去除迭代过程中的相关性，也就是在迭代中间会跳过sample lag间隔
	private static int SAMPLE_LAG;
	private static int dispcol = 0;
	
	/**
	 * Initialise the Gibbs sampler with data.
	 * @param docs
	 * @param V
	 */
	public GibbsSampler(int[][] docs, int V) {
		this.docs = docs;
		this.M = docs.length;
		this.V = V;
	}
	
	public void configure(int iterations, int burnIn, int thinInterval, int sampleLag) {
		this.ITERATIONS = iterations;
		this.BURN_IN = burnIn;
		this.THIN_INTEVAL = thinInterval;
		this.SAMPLE_LAG = sampleLag;
	}
	
	/**
	 * Initialisation: Must start with an assignment of observations to topics and
     * Many alternatives are possible, I chose to perform random assignments
     * with equal probabilities
     * 
	 * @param K
	 */
	public void initialState(int K) {
		int i = 0;
		int M = docs.length;
		
		// initialize the matrix
		z = new int[M][];
		nw = new int[V][K];
		nd = new int[M][K];
		
		// init the array
		nwsum = new int[K];
		ndsum = new int[M];
		
		// 初始化的时候随机分配一个话题
		// 相当于一个先验过程
		for (int m=0; m<M; m++) {
			int N = docs[m].length;
			z[m] = new int[N];
			for (int n=0; n<N; n++) {
				// 随机分配一个主题（相当于论文里面的抛硬币）
				int topic = (int) (Math.random() * K);
				z[m][n] = topic;
				// 增加该单词在topic的统计
				// docs[m][n] 是第m个doc中的第n个词 ，值为这个词在V向量中的位置（编号）
				nw[docs[m][n]][topic]++;
				// 增加该文章在topic的统计
				nd[m][topic]++;
				// 增加该topic在总的topics中的统计
				nwsum[topic]++;
			}
			// 获得该文章的词统计
			ndsum[m] = N;
		}
	}
	
	/**
	 * Main method: Select initial state ? Repeat a large number of times: 1. 
     * Select an element 2. Update conditional on other elements. If 
     * appropriate, output summary for each run. 
     * 
	 * @param K
	 * @param alpha symmetric prior parameter on docs--topics association
	 * @param beta symmetric prior parameter on topic--term associations
	 */
	public void gibbs(int K, double alpha, double beta) {
		this.K = K;
		// α参数
		this.alpha = alpha;
		// β参数 用来
		this.beta = beta;
		
		//
		if (SAMPLE_LAG > 0) {
			// 文章中主题的分布
			thetasum = new double[M][K];
			// 主题中每个词的分布
			phisum = new double[K][V];
			numstats = 0;
		}
		
		// 随机初始化初始状态
		initialState(K);
		
		// 开始每一轮的gibbs sample
		for (int i=0; i<this.ITERATIONS; i++) {
			// 开始具体的gibbs sample
			for (int m=0; m<z.length; m++) {
				for (int n=0; n<z[m].length; n++) {
					// 核心步骤
					// "Gibbs Sample for the Uninitiate" 2.5.1
					// "Parameter Estimation for Text Analysis" 5.5
					int topic = sampleFullCondition(m, n);
					z[m][n] = topic;
				}
			}
			
			// get statics after burn-in stage
			// 通过burn-in使得模型达到稳定态
			// sample lag使得模型decorrelated
			if ((i > this.BURN_IN) && (this.SAMPLE_LAG > 0) && (i % this.SAMPLE_LAG == 0)) {
				updateParams();
			}
		}
	}
	
	public int sampleFullCondtion(int m, int n) {
		int topicRet = z[m][n];
		
		return topicRet;
	}
	
 	public static void main(String[] args) {
		System.out.println(Math.random());
	}

}
