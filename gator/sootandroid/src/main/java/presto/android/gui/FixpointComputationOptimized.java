/*
 * FixpointComputationOptimized.java - part of the GATOR project
 *
 * Copyright (c) 2019 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */

package presto.android.gui;

import com.google.common.collect.Sets;
import presto.android.Configs;
import presto.android.Debug;
import presto.android.Logger;
import presto.android.MultiMapUtil;
import presto.android.gui.graph.*;
import soot.jimple.toolkits.scalar.NopEliminator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class include two methods:
 * 1) optimized computePathsFromViewProducerToViewConsumer() of FixpointSolver.
 * 2) modified (to support the optimization) windowReachability() of FixpointSolver.
 * Calls to these two methods are changed to these in here.
 */
public class FixpointComputationOptimized {
    static void optimizedComputePathsFromViewProducerToViewConsumer(FixpointSolver solver) {
        for (NNode source : solver.flowgraph.allNNodes) {
            Set<NNode> reachables = null;
            if (source instanceof NViewAllocNode
                    || source instanceof NInflNode
                    || source instanceof NOptionsMenuNode
                    || source instanceof NContextMenuNode
                    || source instanceof NFindView1OpNode
                    || source instanceof NFindView2OpNode
                    || source instanceof NFindView3OpNode
                    || source instanceof NInflate1OpNode) {
                reachables = solver.graphUtil.reachableNodes(source);
                for (NNode target : reachables) {
                    if (!(target instanceof NOpNode)) {
                        continue;
                    }
                    //NOpNode opNode = (NOpNode) target;
                    // View as parameter
                    if (target instanceof NAddView1OpNode
                            || (target instanceof NAddView2OpNode
                                && reachables.contains(((NOpNode) target).getParameter()))) {
                        MultiMapUtil.addKeyAndHashSetElement(
                                solver.reachingParameterViews, (NOpNode) target, source);
                        if (source instanceof NOpNode) {
                            //reverse
                            MultiMapUtil.addKeyAndHashSetElement(solver.reachedParameterViews,
                                    (NOpNode) source, (NOpNode) target);
                        } else if (source instanceof NObjectNode
                                && solver.isValidFlowByType(source, (NOpNode) target,
                                    FixpointSolver.VarType.Parameter)) {
                            if (source instanceof NViewAllocNode
                                    || source instanceof NInflNode) {
                                MultiMapUtil.addKeyAndHashSetElement(solver.solutionParameters,
                                        (NOpNode) target, source);
                            } else {
                                if (Configs.sanityCheck) {
                                    throw new RuntimeException(
                                            "Unhandled reaching parameter at " + target + " for " + source);
                                } else {
                                    Logger.warn(FixpointComputationOptimized.class.getSimpleName(),
                                            "Unhandled reaching parameter at " + target + " for " + source);
                                }
                            }
                        }
                    } else if (target instanceof NSetListenerOpNode
                            && reachables.contains(((NSetListenerOpNode) target).getParameter())) {
                        if (source instanceof NOpNode) {
                            //the result of source could flow to SetListener
                            //If source is NObjectNode, it will be taken care of later.
                            MultiMapUtil.addKeyAndHashSetElement(solver.reachingListeners,
                                    ((NSetListenerOpNode) target), source);
                            //reverse
                            MultiMapUtil.addKeyAndHashSetElement(solver.reachedListeners,
                                    (NOpNode) source, (NSetListenerOpNode) target);
                        }
                    }
                    // View as receiver
                    if (target instanceof NFindView1OpNode
                            || target instanceof NFindView3OpNode
                            || target instanceof NSetIdOpNode
                            || target instanceof NSetTextOpNode
                            || (target instanceof NSetListenerOpNode
                                && reachables.contains(((NOpNode) target).getReceiver()))
                            || (target instanceof NAddView2OpNode
                                && reachables.contains(((NOpNode) target).getReceiver()))) {
                        if ((source instanceof NOptionsMenuNode
                                    || source instanceof NContextMenuNode)
                                && target instanceof NSetIdOpNode) {
                            //MenuNode cannot be a receiver of SetId
                            continue;
                        }
                        MultiMapUtil.addKeyAndHashSetElement(solver.reachingReceiverViews,
                                (NOpNode) target, source);
                        if (source instanceof NOpNode) {
                            //reverse
                            MultiMapUtil.addKeyAndHashSetElement(solver.reachedReceiverViews,
                                    (NOpNode) source, (NOpNode) target);
                        } else if (source instanceof NObjectNode
                                && solver.isValidFlowByType(source, (NOpNode) target,
                                    FixpointSolver.VarType.Receiver)) {
                            if (source instanceof NViewAllocNode
                                    || source instanceof NOptionsMenuNode
                                    || source instanceof NContextMenuNode
                                    || source instanceof NInflNode) {
                                MultiMapUtil.addKeyAndHashSetElement(solver.solutionReceivers,
                                        (NOpNode) target, source);
                            } else {
                                if (Configs.sanityCheck) {
                                    throw new RuntimeException(
                                            "Unhandled reaching receiver at " + target + " for " + source);
                                } else {
                                    Logger.warn(FixpointComputationOptimized.class.getSimpleName(),
                                            "Unhandled reaching receiver at " + target + " for " + source);
                                }
                            }
                        }
                    }
                }
            }
            // Any object could be a listener
            if (source instanceof NObjectNode
                    && solver.listenerSpecs.isListenerType(((NObjectNode) source).getClassType())) {
                if (reachables == null) {
                    reachables = solver.graphUtil.reachableNodes(source);
                }
                for (NNode target : reachables) {
                    if (target instanceof NSetListenerOpNode
                            && reachables.contains(((NSetListenerOpNode) target).getParameter())) {
                        MultiMapUtil.addKeyAndHashSetElement(solver.reachingListeners,
                                ((NSetListenerOpNode) target), source);
                        MultiMapUtil.addKeyAndHashSetElement(solver.solutionListeners,
                                (NSetListenerOpNode) target, source);
                    }
                }
            }
        }
        solver.solutionResultsReachability();
    }

    static void windowReachability(FixpointSolver solver) {
        for (NWindowNode windowNode : NWindowNode.windowNodes) {
            Set<NNode> reachables = solver.graphUtil.reachableNodes(windowNode);
            for (NNode target : reachables) {
                if (!(target instanceof NOpNode)) {
                    continue;
                }
                NOpNode opNode = (NOpNode) target;
                if ((opNode instanceof NInflate2OpNode
                        || opNode instanceof NAddView1OpNode
                        || opNode instanceof NFindView2OpNode)
                        && reachables.contains(opNode.getReceiver())) {
                    MultiMapUtil.addKeyAndHashSetElement(solver.reachingWindows, opNode, windowNode);
                } else if (opNode instanceof NSetListenerOpNode
                        && reachables.contains(opNode.getParameter())) {
                    if (Configs.debugCodes.contains(Debug.LISTENER_DEBUG)) {
                        Logger.verb(FixpointComputationOptimized.class.getSimpleName(),
                                "[WindowAsListener] " + windowNode + " -> " + opNode);
                    }
                    MultiMapUtil.addKeyAndHashSetElement(solver.reachingListeners, opNode, windowNode);
                    if (solver.listenerSpecs.isListenerType((windowNode).getClassType())) {
                        MultiMapUtil.addKeyAndHashSetElement(solver.solutionListeners, opNode, windowNode);
                    }
                } else {
                    //throw new RuntimeException(objectNode + " reaching " + opNode);
                }
            }
        }
    }
}
