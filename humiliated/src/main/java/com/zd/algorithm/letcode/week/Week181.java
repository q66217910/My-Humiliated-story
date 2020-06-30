package com.zd.algorithm.letcode.week;

import com.google.common.collect.Lists;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Week181 {

    public int maxPower(String s) {
        int j = 0, count = 1, result = 1;
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == s.charAt(j)) {
                count++;
            } else {
                j = i;
                count = 1;
            }
            result = Math.max(result, count);
        }
        return result;
    }

    public List<String> simplifiedFractions(int n) {
        List<String> list = new ArrayList<>();
        if (n > 1) {
            for (int i = 2; i <= n; i++) {
                int a = i - 1;
                while (a >= 1) {
                    if (new BigInteger(a + "").gcd(new BigInteger(i + "")).intValue() == 1) {
                        list.add(a + "/" + i);
                    }
                    a--;
                }
            }
        }
        return list;
    }

    public String largestNumber(int[] cost, int target) {
        int[] f = new int[target + 1];
        Arrays.fill(f, -1000);
        f[0] = 0;
        for (int i = 1; i <= target; i++) {
            for (int j = 1; j <= 9; j++) {
                int c = cost[j - 1];
                if (i >= c) {
                    f[i] = Math.max(f[i], f[i - c] + 1);
                }
            }
        }
        if (f[target] < 0) {
            return "0";
        } else {
            StringBuilder ret = new StringBuilder();
            int now = target;
            while (now > 0) {
                for (int i = 9; i >= 1; i--) {
                    int c = cost[i - 1];
                    if (now >= c && f[now] == f[now - c] + 1) {
                        ret.append(i);
                        now -= c;
                        break;
                    }
                }
            }
            return ret.toString();
        }
    }

    public int busyStudent(int[] startTime, int[] endTime, int queryTime) {
        int res = 0;
        for (int i = 0; i < startTime.length; i++) {
            if (startTime[i] <= queryTime && endTime[i] >= queryTime) res++;
        }
        return res;
    }

    public String arrangeWords(String text) {
        //根据空格分隔
        String[] s = text.split(" ");
        String collect = Arrays.stream(s)
                .sorted(Comparator.comparing(String::length))
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
        char[] chars = collect.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    class Node {
        Set<String> set;
        Node next;
        int id;

        Node(Set<String> set) {
            this.set = set;
        }
    }

    public List<Integer> peopleIndexes(List<List<String>> favoriteCompanies) {
        Node head = new Node(null);
        Node tail = head;
        Map<String, Set<Node>> map = new HashMap<>();
        for (int i = 0; i < favoriteCompanies.size(); i++) {
            List<String> coll = favoriteCompanies.get(i);
            Set<String> set = new HashSet<>();
            Node node = new Node(set);
            node.id = i;
            tail.next = node;
            tail = tail.next;

            for (String cmp : coll) {
                set.add(cmp);
                map.compute(cmp, (k, v) -> {
                    Set<Node> cset = v;
                    if (cset == null) {
                        cset = new HashSet<>();
                    }
                    cset.add(node);
                    return cset;
                });
            }
        }
        List<Integer> list = new ArrayList<>();
        Node n = head;
        while ((n = n.next) != null) {
            if (n.set.size() == 0) {
                continue;
            }

            Iterator<String> iterator = n.set.iterator();
            String cmp = iterator.next();
            Set<Node> cset = map.get(cmp);
            if (cset.size() == 1) {
                list.add(n.id);
                continue;
            }
            boolean contain = false;
            for (Node node : cset) {
                if (node == n) {
                    continue;
                }
                boolean c = true;
                for (String icmp : n.set) {
                    if (!node.set.contains(icmp)) {
                        c = false;
                        break;
                    }
                }
                if (c) {
                    contain = true;
                    break;
                }
            }

            if (!contain) {
                list.add(n.id);
            }
        }
        return list;
    }

    private int r, num;

    public int numPoints(int[][] p, int r) {
        if (p == null) {
            return 0;
        } else if (p.length <= 1) {
            return p.length;
        }
        this.r = r;
        this.num = p.length;
        int res = 1;
        for (int i = 0; i < num; ++i) {
            for (int j = i + 1; j < num; ++j) {
                if (dist(p[i][0], p[i][1], p[j][0], p[j][1]) > 2 * r) {
                    continue;
                }
                double[] center = getCircleCenter(p[i][0], p[i][1], p[j][0], p[j][1]);
                int cnt = 0;
                for (int k = 0; k < num; ++k) {
                    if (dist(center[0], center[1], p[k][0], p[k][1]) < r + 0.000001) {
                        ++cnt;
                    }
                }
                res = Math.max(res, cnt);
            }
        }
        return res;
    }

    private double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    private double[] getCircleCenter(double x1, double y1, double x2, double y2) {
        double m1 = (x1 + x2) / 2, m2 = (y1 + y2) / 2;
        double angle = Math.atan2(x1 - x2, y2 - y1);
        double d = Math.sqrt(r * r - Math.pow(dist(x1, y1, m1, m2), 2));
        return new double[]{m1 + d * Math.cos(angle), m2 + d * Math.sin(angle)};
    }

    public int countNum(int x, int y, int r, int[][] points) {
        int count = 0;
        for (int[] point : points) {
            if (Math.pow(Math.pow(point[0] - x, 2) + Math.pow(point[1] - y, 2), 0.5) <= r) {
                count++;
            }
        }
        return count;
    }

    public int isPrefixOfWord(String sentence, String searchWord) {
        String[] sentences = sentence.split(" ");
        for (int i = 0; i < sentences.length; i++) {
            boolean flag = true;
            if (sentences[i].length() < searchWord.length()) {
                continue;
            }
            for (int j = 0; j < searchWord.length(); j++) {
                if (sentences[i].charAt(j) != searchWord.charAt(j)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                return i + 1;
            }
        }
        return -1;
    }

    public int maxVowels(String s, int k) {
        int result = 0, temp = 0;
        for (int i = 0; i < k; i++) {
            if (isA(s.charAt(i))) {
                temp++;
            }
        }
        result = temp;
        for (int i = k; i < s.length(); i++) {
            if (isA(s.charAt(i - k))) temp--;
            if (isA(s.charAt(i))) temp++;
            result = Math.max(result, temp);
        }
        return result;
    }

    private boolean isA(char a) {
        switch (a) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                return true;
        }
        return false;
    }

    public int maxDotProduct(int[] nums1, int[] nums2) {
        int m = nums1.length;
        int n = nums2.length;
        int[][] dp = new int[m][n];
        for (int i = 0; i < m; i++) {
            Arrays.fill(dp[i], -100000000);
        }
        int ret = -100000000;
        dp[0][0] = nums1[0] * nums2[0];
        //初始化
        for (int i = 1; i < n; i++) {
            //选择一个时的最大值
            dp[0][i] = Math.max(dp[0][i - 1], nums1[0] * nums2[i]);
            ret = Math.max(dp[0][i], ret);
        }
        for (int i = 1; i < m; i++) {
            //选择一个时的最大值
            dp[i][0] = Math.max(dp[i - 1][0], nums1[i] * nums2[0]);
            ret = Math.max(dp[i][0], ret);
        }
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                //去掉num1当前，或者去掉num2当前，或者只取nums1[i] * nums2[j]，或者前一个最大值加上当前乘积
                dp[i][j] = Math.max(dp[i][j - 1],
                        Math.max(dp[i - 1][j],
                                Math.max(dp[i - 1][j - 1] + nums1[i] * nums2[j], nums1[i] * nums2[j])));
                ret = Math.max(dp[i][j], ret);
            }
        }
        return ret;
    }

    public int[] shuffle(int[] nums, int n) {
        int[] result = new int[2 * n];
        for (int i = 0, j = 0; i < n; i++, j += 2) {
            result[j] = nums[i];
            result[j + 1] = nums[n + i];
        }
        return result;
    }

    public int[] getStrongest(int[] arr, int k) {
        List<Integer> list = new ArrayList<>();
        Arrays.sort(arr);
        //中位数
        int m = arr[(arr.length - 1) / 2];
        int i = 0, j = arr.length - 1;
        while (list.size() < k) {
            if (Math.abs(arr[i] - m) > Math.abs(arr[j] - m)) {
                list.add(arr[i]);
                i++;
            } else {
                list.add(arr[j]);
                j--;
            }
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    public int minCost(int[] houses, int[][] cost, int m, int n, int target) {
        int ans = 100000;
        //i:房子数 ，j：街区数， k：装修成什么
        int[][][] dp = new int[m + 1][m + 1][n + 1];
        for (int i = 0; i < m + 1; i++) {
            for (int j = 0; j < m + 1; j++) {
                Arrays.fill(dp[i][j], 100000);
            }
        }
        dp[0][0][0] = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j <= i; j++) {
                for (int k = 0; k <= n; k++) {
                    //说明不需要装修
                    if (houses[i] > 0) {
                        //j + (k != houses[i] ? 1 : 0): 装修成K是不是会多一个街区
                        // 因为不需要装修，所以没有花费，取最小值就好
                        dp[i + 1][j + (k != houses[i] ? 1 : 0)][houses[i]]
                                = Math.min(dp[i + 1][j + (k != houses[i] ? 1 : 0)][houses[i]], dp[i][j][k]);
                    } else {
                        //装修
                        for (int l = 1; l <= n; l++) {
                            dp[i + 1][j + (k != l ? 1 : 0)][l] = Math.min(dp[i + 1][j + (k != l ? 1 : 0)][l], dp[i][j][k] + cost[i][l - 1]);
                        }
                    }
                }
            }
        }
        //取街全部装修完的最小费用
        for (int l = 1; l <= n; l++) {
            ans = Math.min(ans, dp[m][target][l]);
        }
        return ans == 100000 ? -1 : ans;
    }

    public int[] finalPrices(int[] prices) {
        int n = prices.length;
        int[] res = new int[n];
        for (int i = 0; i < n; i++) {
            boolean ret = true;
            for (int j = i + 1; j < n; j++) {
                if (prices[j] <= prices[i]) {
                    res[i] = prices[i] - prices[j];
                    ret = false;
                    break;
                }
            }
            if (ret) {
                res[i] = prices[i];
            }
        }
        return res;
    }

    public int minSumOfLengths(int[] arr, int target) {
        //总数对应的索引
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, 0);
        //数字的总和
        int sum = 0;
        int n = arr.length, ans = 0x3f3f3f3f;
        //dp:表示当前最小值
        int[] dp = new int[100005];
        Arrays.fill(dp, 0x3f);
        for (int i = 1; i <= n; i++) {
            sum += arr[i - 1];
            //剩余数
            int gp = sum - target;
            //若当前值没有默认前一个值
            dp[i] = dp[i - 1];
            //若存在数gp则表示从  pos -> i 是一个子数组
            if (map.containsKey(gp)) {
                int pos = map.get(gp);
                //设置dp的值
                dp[i] = Math.min(dp[i], i - pos);
                //ans为当前加上到pos的最小值
                ans = Math.min(ans, i - pos + dp[pos]);
            }
            map.put(sum, i);
        }
        return ans >= 0x3f ? -1 : ans;
    }

    public int minDistance(int[] houses, int k) {
        Arrays.sort(houses);
        int n = houses.length;
        //总距离
        int[] s = new int[n + 1];
        for (int i = 0; i < n; i++) s[i + 1] = s[i] + houses[i];
        //第i房子和j房子只有一个邮箱的差距
        int[][] w = new int[n + 1][n + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = i; j <= n; j++) {
                w[i][j] = s[j] - s[i + j - 1 >> 1] - s[i + j >> 1] + s[i - 1];
            }
        }
        //表示i栋房子在k个邮箱的最小值
        int[][] dp = new int[n + 1][k + 1];
        for (int[] ints : dp) {
            Arrays.fill(ints, 10000);
        }
        dp[0][0] = 0;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= i && j <= k; j++) {
                for (int l = 0; l < i; l++) {
                    //表示 从l个j-1个的最小值+ 多一个邮箱(l+1到i只有1个邮箱)
                    dp[i][j] = Math.min(dp[i][j], dp[l][j - 1] + w[l + 1][i]);
                }
            }
        }
        return dp[n][k];
    }

    public int[] runningSum(int[] nums) {
        int[] res = new int[nums.length];
        int sum = 0;
        for (int i = 0; i < nums.length; i++) {
            sum += nums[i];
            res[i] = sum;
        }
        return res;
    }

    public int findLeastNumOfUniqueInts(int[] arr, int k) {
        Map<Integer, Long> map = Arrays.stream(arr).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        int result = map.size();
        List<Long> list = map.values().stream().sorted().collect(Collectors.toList());
        for (Long value : list) {
            if (k >= value) {
                result--;
                k -= value;
            }
        }
        return result;
    }

    public int minDays(int[] bloomDay, int m, int k) {
        if (m * k > bloomDay.length) {
            return -1;
        }
        // 最大等待的天数是数组里的最大值
        int max = 0;
        for (int i : bloomDay) {
            max = Math.max(max, i);
        }
        // 最小等待0天
        int min = 0;
        while (min < max) {
            // mid:等待天数
            int mid = min + (max - min) / 2;
            // 等待mid天，有多少组连续的k朵花已经开花🌼了
            int count = getCount(bloomDay, mid, k);
            if (count >= m) {
                max = mid;
            } else {
                min = mid + 1;
            }
        }
        return min;
    }

    // 返回等待day天，有多少组连续的k天<=day  这里用的贪心
    private int getCount(int[] arr, int day, int k) {
        int re = 0;
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] <= day) {
                count++;
            } else {
                count = 0;
            }
            //  连续的k朵花🌼开了
            if (count == k) {
                re++;
                count = 0;
            }
        }
        return re;
    }

    public double average(int[] salary) {
        Arrays.sort(salary);
        double sum = 0;
        for (int i = 1; i < salary.length - 1; i++) {
            sum += salary[i];
        }
        return sum / (salary.length - 2);
    }

    public int kthFactor(int n, int k) {
        int count = 0;
        for (int i = 1; i <= n; i++) {
            if (n % i == 0) {
                count++;
            }
            if (count == k) {
                return i;
            }
        }
        return -1;
    }

    public int longestSubarray(int[] nums) {
        //从左到右
        int[] dp1 = new int[nums.length];
        //从右到左
        int[] dp2 = new int[nums.length];
        dp1[0] = nums[0] == 1 ? 1 : 0;
        dp2[nums.length - 1] = nums[nums.length - 1] == 1 ? 1 : 0;
        for (int i = 1; i < nums.length; i++) {
            dp1[i] = nums[i] == 1 ? dp1[i - 1] + 1 : 0;
            dp2[nums.length - i - 1] = nums[nums.length - i - 1] == 1 ? dp2[nums.length - i] + 1 : 0;
        }

        int max = 0;
        for (int i = 0; i < nums.length; i++) {
            max = Math.max(max, (i - 1 >= 0 ? dp1[i - 1] : 0) + (i + 1 < nums.length ? dp2[i + 1] : 0));
        }
        return max;
    }

    public int minNumberOfSemesters(int n, int[][] dependencies, int k) {
        //入度
        ArrayList<Integer>[] graph = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
        }
        int[] in = new int[n + 1];
        for (int[] dependency : dependencies) {
            graph[dependency[0] - 1].add(dependency[1] - 1);
            in[dependency[1] - 1]++;
        }

        PriorityQueue<Integer> queue = new PriorityQueue<>((a, b) -> in[b] - in[a]);
        for (int i = 0; i < n; i++) {
            if (in[i] == 0) {
                queue.add(i);
            }
        }

        int num = 0;
        while (!queue.isEmpty()) {
            ArrayList<Integer> next = new ArrayList<>();
            for (int i = 0; !queue.isEmpty() && i < k; i++) {
                int j = queue.remove();
                for (int l : graph[j]) {
                    in[l]--;
                    if (in[l] == 0) {
                        next.add(l);
                    }
                }
            }
            queue.addAll(next);
            num++;
        }
        return num;
    }

    public boolean isPathCrossing(String path) {
        int x = 0, y = 0;
        Set<String> set = new HashSet<>();
        set.add(x + "" + y);
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == 'N') {
                y++;
            } else if (path.charAt(i) == 'S') {
                y--;
            } else if (path.charAt(i) == 'E') {
                x++;
            } else if (path.charAt(i) == 'W') {
                x--;
            }
            if (set.contains(x + "" + y)) {
                return true;
            }
            set.add(x + "" + y);
        }
        return false;
    }

    public boolean canArrange(int[] arr, int k) {
        int[] modArr = new int[k];
        for (int i = 0; i < arr.length; i++) {
            int mod = arr[i] % k;
            //如果余数是负数，则使用k+mod作为index
            mod = mod >= 0 ? mod : k + mod;
            modArr[mod] += 1;
        }
        if (modArr[0] % 2 != 0) {
            return false;
        }
        for (int i = 1; i < k / 2; i++) {
            if (modArr[i] != modArr[k - i]) {
                return false;
            }
        }
        return true;
    }

    public int numSubseq(int[] nums, int target) {
        Arrays.sort(nums);
        if (nums[0] * 2 > target) {
            return 0;
        }
        int left = 0;
        int right = nums.length - 1;
        int res = 0;
        int[] pow = new int[nums.length];
        pow[0] = 1;
        int mode = 1_0000_0000_7;
        for (int i = 1; i < nums.length; i ++) {
            pow[i] = pow[i-1] * 2;
            pow[i] %= mode;
        }
        while (left <= right) {
            if (nums[left] + nums[right] <= target) {
                res += pow[right - left];
                res %= mode;
                left ++;
            }
            else {
                right --;
            }
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(new Week181().canArrange(new int[]{-1014622,654186,631765,55143,-479170,723779,960331,138287,-294164,824557,-170875,-1002135,-398220,-578783,-628356,-359739,919698,490673,820000,-707407,231153,-460838,-323412,-422290,-940172,-328029,-309953,-745191,-181528,-238875,-732625,669725,969786,-133707,372369,283806,312263,36866,846465,566121,201332,751013,-103468,-397527,-597623,-27642,-945020,581139,-972355,-739932,942915,366723,43229,864376,412540,451331,-130355,240227,607310,986181,774476,-695039,38146,878909,-801109,-82721,-316039,-605337,-791724,-605689,178571,293765,-5416,80446,519807,-792818,461643,-188683,-805021,-229280,84521,-1016022,230428,-541484,-94779,770169,-26420,880886,545570,984941,-751956,348548,-401077,-5752,-607367,979893,-824301,-58199,-513880,-164373,434514,-127719,519331,157948,917632,-306883,378769,959857,901795,705189,450437,-601869,839362,657385,-620544,126164,241769,-589072,388647,-174014,821586,-599213,321511,912783,868145,149464,752550,-575693,-889990,641079,-121773,977767,893287,671109,542644,450343,-303318,164259,-859568,-738754,-440915,110077,780643,366170,161612,-179374,134909,-32599,-859265,-483431,401793,754895,-45172,440372,-220384,-870510,-839529,307899,941518,1849,448694,-970851,714007,-272089,-749864,-548321,-529736,777813,-174845,81495,-188459,492535,446298,885818,-221975,-908730,-419717,38580,-904557,-17646,-353209,89415,266504,-554126,95713,943878,423377,937969,-171570,-91939,-231138,756920,-776885,360812,-819945,-333740,-788630,772431,826347,-385982,-648758,-346763,594078,-645527,-854281,-750827,462862,-348961,-734233,-400842,-55461,558100,466757,-852087,2617,-416708,786730,683961,854245,780894,-45495,54432,641792,-20641,-313510,278578,-910519,-282460,390764,-129120,530647,199167,761793,968484,9564,849886,790073,955096,503666,578708,-199771,144769,276215,771467,-141900,-411435,254689,324656,-648475,-807019,452781,-230363,-19870,-747232,177762,-725173,81280,635214,665373,-151812,434414,893001,-869366,464866,-65763,-714615,212573,-133634,625902,576146,-199370,-383166,-209481,-32318,865362,483572,-828140,406664,-95977,663654,607464,764841,-955150,-54005,-841102,-977491,-945730,-495960,922058,710874,-984138,-600728,-996030,-1002521,-882093,-395755,137843,-37199,260271,416987,319128,-247401,-651963,-575333,-994629,938404,415120,-725642,272952,321556,-321566,833772,-607475,633792,399296,579552,-435292,-761958,-733586,149044,771987,-228001,-265735,-354155,972344,493901,-991604,-716868,656285,-354968,223731,362093,978041,934457,-282357,-309940,-234159,-657489,104403,-638775,35916}, 51054));
    }
}
