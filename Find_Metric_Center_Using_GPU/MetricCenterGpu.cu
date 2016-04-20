//******************************************************************************
//
// File:  MetricCenterGpu.cu
// This file is a part of project 3 of the course:Foundation of Parallel Computing,
// under taken in Fall 2015 at Rochester Institute of Technology. 
//******************************************************************************

#include <float.h>

//Threads per block
#define ThreadsPerBlock 1024

//Structre for storing point with x,y cordinates
typedef struct{
	double x;
	double y;
	}
	point_t;
	
//Structure for storing reduced metric center point's radius and index	
typedef struct{
	double radius;
	int index;
	}reducedRadius_t;
	
//Find distance between two points; 
//square_root( (b1-a1)^2 + (b2-a2)^2 ).
//Returns distance between points a and b
__device__ double pointDistance
	(point_t *a, point_t *b){
		return sqrt( ( (b->x - a->x)*(b->x - a->x)+
			(b->y - a->y)*(b->y - a->y)));
			 
	}

//Find and save min radius and index;
//min( a->radius , b->radius ) or if no min, min( a->index , b->index )	
__device__ void saveMinReducedRadius
	(reducedRadius_t *a, reducedRadius_t *b){
		if( a->radius > b->radius ){
			a->radius = b->radius;
			a->index = b->index;
		}else if( a->radius == b->radius ){
			if( a->index > b->index ){
				a->index = b->index;
			}
		}
	}
//Find and save max radius and index;
//max(a->radius, b->radius)
__device__ void saveMaxReducedRadius
	(reducedRadius_t *a, reducedRadius_t *b){
		if( a->radius < b->radius ){
			a->radius = b->radius;
			a->index = b->index;
		}
	}

//Per thread shared variables	
__shared__ reducedRadius_t shrdMaxRadius [ThreadsPerBlock];



/**
 * kernel program for find metric center from given points
 * This kernel function is called with 1-D grid of size same
 * as number of multiprocessor and 1-D blocks with number of 
 * threads as ThreadsPerBlock.
 *
 * @param  points   Array of points.
 * @param  size     Length of points array.
 * @param  minOfMaxRadius     Array for saving reduced radius and Index.
 *
 * @author  Karan Bhagat
 * @version 6-Nov-2015
 */
extern "C" __global__ void findMetricCenter
	(point_t *points, int size, reducedRadius_t *minOfMaxRadius){

	int blockNum = blockIdx.x;
	int totalBlocks = gridDim.x;	
	int thrdIdx = threadIdx.x;  // Index of this thread in a block
	
	minOfMaxRadius[blockNum].radius = DBL_MAX;
	minOfMaxRadius[blockNum].index = 0; 
	
	reducedRadius_t currentMaxRadius = {0.0,0};

	//find max possible distance of points and then find point with minimum radius.	
	for( int i = blockNum ; i < size; i += totalBlocks ){
		
		shrdMaxRadius[thrdIdx].radius = 0.0;
		shrdMaxRadius[thrdIdx].index = 0;
		
		currentMaxRadius.index = i;
		
		for( int j = thrdIdx ; j < size; j += ThreadsPerBlock ){
			if( j == i ) continue;
			currentMaxRadius.radius = pointDistance( &points[i], &points[j]);
			saveMaxReducedRadius(&shrdMaxRadius[thrdIdx],&currentMaxRadius);
		}
		__syncthreads();
		
		//in block reduction for each point
		for( int i = ThreadsPerBlock/2; i > 0; i >>= 1){
			if( thrdIdx < i ){
				saveMaxReducedRadius(&shrdMaxRadius[thrdIdx],&shrdMaxRadius[thrdIdx+i]);
			
			}
			__syncthreads();
		}
		
		//saving min radius found in block
		if( thrdIdx == 0 ){
			saveMinReducedRadius(&minOfMaxRadius[blockNum], &shrdMaxRadius[thrdIdx]);
		}
	}	
}
	