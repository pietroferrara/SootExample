package com.juliasoft.soot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.MethodOrMethodContext;
import soot.Scene;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.options.Options;

public class CallGraphInspector {

	//Call it passing as arguments:
	//<ClassToBeAnalyzed> -cp <jar_with_class_toBeAnalyzed>;"<JAVA_HOME>\jre\lib\rt.jar";"<JAVA_HOME>\jre\lib\jce.jar"
	//In my example
	//TestCases -cp .\toBeAnalyzed\TestCases.jar;"C:\Program Files\Java\jdk1.8.0_77\jre\lib\rt.jar";"C:\Program Files\Java\jdk1.8.0_77\jre\lib\jce.jar"
	public static void main(String[] args) {
		//Just to avoid to specify yet another option from the command line
		Options.v().set_whole_program(true);
		
		//Invoke Soot like from the command line
		soot.Main.main(args);
		
		//Get the call graph and process it
		CallGraph cg = Scene.v().getCallGraph();
		processCallGraph(cg);
	}

	private static void processCallGraph(CallGraph cg) {
		//Iterate over all methods calling other methods
		Iterator<MethodOrMethodContext> it = cg.sourceMethods();
		while(it.hasNext()) {
			MethodOrMethodContext method = it.next();
			//Filter out only the methods that we believe they are interesting
			if(isInteresting(method.method().getDeclaringClass().getName())) {
				//Check if starting from the given method we can come back to the method itself,
				//meaning that the method is potentially recursive.
				if(isMethodReachable(method, new HashSet<>(), cg))
					System.out.println("WARNING: Method "+method.method().getSignature()+" is (directly or indirectly) recursive");
				else System.out.println("Method "+method.method().getSignature()+" is NOT recursive");
			}
		}
	}

	//Return true iff the given method is not reachable from the called methods
	private static boolean isMethodReachable(
			MethodOrMethodContext method, 
			HashSet<MethodOrMethodContext> alreadyProcessed,
			CallGraph cg) {
		//Iterate over all methods directly called from the given method
		Iterator<Edge> it2 = cg.edgesOutOf(method);
		while(it2.hasNext()) {
			Edge edge = it2.next();
			
			//Check if the given method is reachable starting from the called method
			MethodOrMethodContext target = edge.getTgt();
			List<MethodOrMethodContext> _entries = new ArrayList<>();
			_entries.add(target);
			ReachableMethods reachables = new ReachableMethods(cg,_entries);    
			reachables.update();
			if(reachables.contains(method))
				return true;
		}
		
		//If not, it is not recursive
		return false;
	}

	
	private static boolean isInteresting(String signature) {
		//HARDCODED!!!
		return signature.startsWith("TestCases");
	}
}
