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
	double alpha; // ������,���ھ��� doc--topic �ֲ� dirichlet parameter (doc--topic association)
	double beta; // �²���,���ھ���topic--word�ֲ� topic--word association
	double[][] thetasum; // �ȷֲ���Ҳ���Ǧ�����������doc--topic����cumulative statistics of ��
	double[][] phisum; // phi�ֲ���Ҳ���ǦȲ���������topic--word����cumulative statistics of phi,ʲô�ķֲ���
	// topic assignments for each word
	// M*N, ��һά��doc���ڶ�ά��word
	int[][] z;
	// nw[i][j] �� word i ���䵽 topic j �ϴ�����ͳ��
	// V*K����һά��word���ڶ�ά��topic
	int[][] nw;
	// nd[i][j] �� doc i �еĴ�  ���䵽topic j �ϴ�����ͳ��
	// M*K����һά��doc���ڶ�ά��topic��Ҳ���Ǧȣ�����dirichlet�ֲ�
	int[][] nd;
	// nwsum[j] total number of words assigned to topic j
	int[] nwsum;
	// ndsum[i] total number of words in doc i
	int[] ndsum;
	// size of statistics����
	int numstats;
	// sampling lag����
	private static int THIN_INTEVAL = 20;
	// burn in period
	// burn in������ǰ�Ļᱻ����
	// burn inĿ����ʹ��sample��ʼǰ����״̬�ﵽһ�����ȶ�״̬
	private static int BURN_IN = 100;
	// max iterations
	private static int ITERATIONS = 1000;
	// sample lag (if -1, only one sample taken)
	// sample���������ȥ�����������е�����ԣ�Ҳ�����ڵ����м������sample lag���
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
		
		// ��ʼ����ʱ���������һ������
		// �൱��һ���������
		for (int m=0; m<M; m++) {
			int N = docs[m].length;
			z[m] = new int[N];
			for (int n=0; n<N; n++) {
				// �������һ�����⣨�൱�������������Ӳ�ң�
				int topic = (int) (Math.random() * K);
				z[m][n] = topic;
				// ���Ӹõ�����topic��ͳ��
				// docs[m][n] �ǵ�m��doc�еĵ�n���� ��ֵΪ�������V�����е�λ�ã���ţ�
				nw[docs[m][n]][topic]++;
				// ���Ӹ�������topic��ͳ��
				nd[m][topic]++;
				// ���Ӹ�topic���ܵ�topics�е�ͳ��
				nwsum[topic]++;
			}
			// ��ø����µĴ�ͳ��
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
		// ������
		this.alpha = alpha;
		// �²��� ����
		this.beta = beta;
		
		//
		if (SAMPLE_LAG > 0) {
			// ����������ķֲ�
			thetasum = new double[M][K];
			// ������ÿ���ʵķֲ�
			phisum = new double[K][V];
			numstats = 0;
		}
		
		// �����ʼ����ʼ״̬
		initialState(K);
		
		// ��ʼÿһ�ֵ�gibbs sample
		for (int i=0; i<this.ITERATIONS; i++) {
			// ��ʼ�����gibbs sample
			for (int m=0; m<z.length; m++) {
				for (int n=0; n<z[m].length; n++) {
					// ���Ĳ���
					// "Gibbs Sample for the Uninitiate" 2.5.1
					// "Parameter Estimation for Text Analysis" 5.5
					int topic = sampleFullCondition(m, n);
					z[m][n] = topic;
				}
			}
			
			// get statics after burn-in stage
			// ͨ��burn-inʹ��ģ�ʹﵽ�ȶ�̬
			// sample lagʹ��ģ��decorrelated
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
