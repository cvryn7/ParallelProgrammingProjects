#include<cstdio>
#include<cstring>
#include<cstdlib>
#include<cctype>

#include<cmath>
#include<iostream>
#include<fstream>
#include<cassert>
#include<string>
#include<vector>
#include<queue>
#include<map>
#include<algorithm>
#include<set>
#include<sstream>
#include<stack>
#include<cassert>

using namespace std;

#define MEM(a,b) memset(a,(b),sizeof(a))
#define MAX(a,b) ((a) > (b) ? (a) : (b))
#define MIN(a,b)  ((a) < (b) ? (a) : (b))
#define MP make_pair
#define pb push_back
#define inf 1000000000
#define   M 1000000007

#define maxn 16
#define maxt 10


typedef long long  LL;
typedef pair<LL,LL> pl;
typedef vector<int> vi;
typedef vector<string> vs;
typedef vector<double> vd;


int n;

// The LCS row can be encoded with a bitmask, computes the next row of the LCS after placing v and the previous row can 
// be decoded from mask
int nextMask(int v,int mask,vi &A)
{
    int n=A.size(),ret=0;
    int lcs[2][20],len=0;
    MEM(lcs,0);
    for(int i=0;i<n;i++) // decodes the LCS row
    {
        if(mask&(1<<i)) ++len;
        lcs[0][i+1]=len;
    }

    for(int i=1;i<=n;i++)
    {
        lcs[1][i]=v==A[i-1] ? 1+lcs[0][i-1] : max(lcs[0][i],lcs[1][i-1]);
        if(lcs[1][i]>lcs[1][i-1]) ret|=(1<<(i-1));
    }

    return ret;
}

int memo[18][1<<16];
int newMask[18][1<<16];

int solve(int b,int mask,int K,int L)
{
    int ret=0;

    if(b>n)
    {
        int lcs=0;
        for(int i=0;i<n;i++) if(mask&(1<<i)) ++lcs;
        return lcs==L;
    }
    if(memo[b][mask]!=-1) return memo[b][mask];
    for(int i=1;i<=K;i++)
    {
        ret += solve(b+1,newMask[i][mask],K,L);
        if( ret>=M) ret %=M;
    }
    return memo[b][mask]=ret;
}

int main()
{
	int m,i,j,k,T,cs=0,t=0,a,b;
	int L,K;
	vi A;

	scanf("%d",&T);
	assert(T>=1 && T<=maxt);

	while(T--)
    {
        cin>>n>>K>>L;
        assert(1<=n && n<=maxn && 1<=L && L<=n && 1<=K && K<=maxn);
        A.clear();

        for(i=0;i<n;i++)
        {
            cin>>k;
            assert(1<=k && k<=K);
            A.pb(k);
        }
        

        for(i=1;i<=K;i++)
            for(j=0;j<(1<<n);j++)
            {
                newMask[i][j]=nextMask(i,j,A);
            }

        MEM(memo,-1);
        int ans=solve(1,0,K,L);
        printf("%d\n",ans);

    }


	return 0;
}
