package org.example

import annotation.tailrec
import com.google.caliper.Param

// a caliper benchmark is a class that extends com.google.caliper.Benchmark
// the SimpleScalaBenchmark trait does it and also adds some convenience functionality
class Benchmark extends SimpleScalaBenchmark {
  
  // to make your benchmark depend on one or more parameterized values, create fields with the name you want
  // the parameter to be known by, and add this annotation (see @Param javadocs for more details)
  // caliper will inject the respective value at runtime and make sure to run all combinations 
  @Param(Array("10", "100", "1000", "10000"))
  val length: Int = 0
  
  var array: Array[Int] = _
  var primes: Array[Boolean] = _
  
  override def setUp() {
    // set up all your benchmark data here
    array = Array.range(0, length)
    primes = Array.fill(length)(true)
  }
  
  // the actual code you'd like to test needs to live in one or more methods
  // whose names begin with 'time' and which accept a single 'reps: Int' parameter
  // the body of the method simply executes the code we wish to measure, 'reps' times
  // you can use the 'repeat' method from the SimpleScalaBenchmark trait to repeat with relatively low overhead
  // however, if your code snippet is very fast you might want to implement the reps loop directly with 'while'
  def timeForeachUnit(reps: Int) = repeat(reps) {
    var result = 0    
    (0 to length).foreach {
      result += _
    }
    assert(result == (length + 1) * length / 2) // no messages, to avoid thunk creation
    result // always have your snippet return a value that cannot easily be "optimized away"
  }
  
  def timeWhileUnit(reps: Int) = repeat(reps) {
    var result = 0
    var i = 0
    while (i <= length) {
      result += i
      i += 1 
    }
    assert(result == (length + 1) * length / 2)
    result
  }

  def timeForeachInt(reps: Int) = repeat(reps) {
    var result = 0    
    (0 to length).foreach { i =>
      result += i
      result
    }
    assert(result == (length + 1) * length / 2)
    result
  }
  
  def timeWhileInt(reps: Int) = repeat(reps) {
    var result = 0
    var i = 0
    while (i <= length) {
      result += i
      i += 1 
      result  // likely to be optimized away?
    }
    assert(result == (length + 1) * length / 2)
    result
  }

  def timeForeachDec(reps: Int) = repeat(reps) {
    var result = 0    
    (length to 0 by -1).foreach {
      result += _
    }
    assert(result == (length + 1) * length / 2)
    result
  }
  
  def timeForeachReverse(reps: Int) = repeat(reps) {
    var result = 0    
    (0 to length reverse).foreach {
      result += _
    }
    assert(result == (length + 1) * length / 2)
    result
  }
  
  def timeWhileDec(reps: Int) = repeat(reps) {
    var result = 0
    var i = length
    while (i >= 0) {
      result += i
      i -= 1 
    }
    assert(result == (length + 1) * length / 2)
    result
  }

  def timeForeachOpen(reps: Int) = repeat(reps) {
    var result = 0    
    (0 until length).foreach {
      result += _
    }
    assert(result == (length - 1) * length / 2)
    result
  }
  
  def timeWhileOpen(reps: Int) = repeat(reps) {
    var result = 0
    var i = 0
    while (i < length) {
      result += i
      i += 1 
    }
    assert(result == (length - 1) * length / 2)
    result
  }

  def timeForeachStep2(reps: Int) = repeat(reps) {
    var result = 0    
    (0 to length by 2).foreach {
      result += _
    }
    assert(result == (length + 2) * length / 4)
    result
  }
  
  def timeWhileStep2(reps: Int) = repeat(reps) {
    var result = 0
    var i = 0
    while (i <= length) {
      result += i
      i += 2 
    }
    assert(result == (length + 2) * length / 4)
    result
  }

  def timeForeachStepM2(reps: Int) = repeat(reps) {
    var result = 0    
    (length to 0 by -2).foreach {
      result += _
    }
    assert(result == (length + 2) * length / 4)
    result
  }
  
  def timeWhileStepM2(reps: Int) = repeat(reps) {
    var result = 0
    var i = length
    while (i >= 0) {
      result += i
      i -= 2 
    }
    assert(result == (length + 2) * length / 4)
    result
  }

  def timeForeachArray(reps: Int) = repeat(reps) {
    var result = 0    
    array.indices.foreach {
      result += array(_)
    }
    assert(result == (length - 1) * length / 2)
    result
  }
  
  def timeWhileArray(reps: Int) = repeat(reps) {
    var result = 0
    var i = 0
    while (i < array.length) {
      result += array(i)
      i += 1 
    }
    assert(result == (length - 1) * length / 2)
    result
  }

  def timeForeachHeron(reps: Int) = repeat(reps) {
    var result = 0.0
    (1 to length).foreach { i =>
      var estimate = i.toDouble
      while (math.abs(i - estimate * estimate) > 0.01) {
        estimate = (estimate + i / estimate) / 2
      }
      result += estimate
    }
    result
  }

  def timeWhileHeron(reps: Int) = repeat(reps) {
    var result = 0.0
    var i = 1
    while(i <= length) {
      var estimate = i.toDouble
      while (math.abs(i - estimate * estimate) > 0.01) {
        estimate = (estimate + i / estimate) / 2
      }
      result += estimate
      i += 1
    }
    result
  }
  // Too slow
  def timeForeachDumbPrime(reps: Int) = repeat(reps) {
    var result = 0
    (2 until primes.length).foreach { i =>
      (2 until i).foreach { j =>
        if (primes(j) && i % j == 0) primes(i) = false
      }
      if (primes(i)) result += 1
    }
    result
  }

  def timeWhileDumbPrime(reps: Int) = repeat(reps) {
    var result = 0
    var i = 2
    while (i < primes.length) {
      var j = 2
      while (j < i) {
        if (primes(j) && i % j == 0) primes(i) = false
        j += 1
      }
      if (primes(i)) result += 1
      i += 1
    }
    result
  }

  def timeForeachEratosthenes(reps: Int) = repeat(reps) {
    var result = 0
    (2 until primes.length).foreach { i =>
      if (primes(i)) (2 until i).foreach { j =>
        if (primes(j) && i % j == 0)
          (i until primes.length by i).foreach { k =>
            primes(k) = false
          }
      }
      if (primes(i)) result += 1
    }
    result
  }

  def timeWhileEratosthenes(reps: Int) = repeat(reps) {
    var result = 0
    var i = 2
    while (i < primes.length) {
      if (primes(i)) {
        var j = 2
        while (j < i) {
          if (primes(j) && i % j == 0) {
            var k = i
            while(k < primes.length) {
              primes(k) = false
              k += i
            }
          }
          j += 1
        }
      }
      if (primes(i)) result += 1
      i += 1
    }
    result
  }

  override def tearDown() {
    // clean up after yourself if required
    array = null
    primes = null
  }
  
}

