/*
 *  Copyright (c) 2012 Sam Harwell, Tunnel Vision Laboratories LLC
 *  All rights reserved.
 *
 *  The source code of this document is proprietary work, and is not licensed for
 *  distribution. For information about licensing, contact Sam Harwell at:
 *      sam@tunnelvisionlabs.com
 */
package org.antlr.works.editor.antlr4.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNConfig;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.atn.AtomTransition;
import org.antlr.v4.runtime.atn.NotSetTransition;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.atn.RangeTransition;
import org.antlr.v4.runtime.atn.SetTransition;
import org.antlr.v4.runtime.atn.SimulatorState;
import org.antlr.v4.runtime.atn.Transition;
import org.antlr.v4.runtime.atn.WildcardTransition;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.IntegerList;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 *
 * @author Sam Harwell
 */
public abstract class AbstractCompletionParserATNSimulator extends ParserATNSimulator<Token> {

    private Map<ATNConfig, List<Transition>> caretTransitions;
    private CaretToken caretToken;

    private List<MultipleDecisionData> decisionPoints;
    private IntegerList selections;

    // state variables used for the custom implementation
    private TokenStream<? extends Token> _input;
    private int _startIndex;
    private ParserRuleContext<Token> _outerContext;

    public AbstractCompletionParserATNSimulator(@NonNull Parser<Token> parser, ATN atn) {
        super(parser, atn);
        Parameters.notNull("parser", parser);
        disable_global_context = true;
    }

    public Map<ATNConfig, List<Transition>> getCaretTransitions() {
        return caretTransitions;
    }

    public CaretToken getCaretToken() {
        return caretToken;
    }

    public Parser<Token> getParser() {
        return parser;
    }

    public void setFixedDecisions(List<MultipleDecisionData> decisionPoints, IntegerList selections) {
        Parameters.notNull("decisionPoints", decisionPoints);
        Parameters.notNull("selections", selections);
        this.decisionPoints = decisionPoints;
        this.selections = selections;
        this.caretTransitions = null;
    }

    @Override
    public int adaptivePredict(TokenStream<? extends Token> input, int decision, ParserRuleContext<Token> outerContext) {
        _input = input;
        _startIndex = input.index();
        _outerContext = outerContext;
        caretTransitions = null;

        if (decisionPoints != null) {
            int index = input.index();
            for (int i = 0; i < decisionPoints.size(); i++) {
                if (decisionPoints.get(i).inputIndex == index && decisionPoints.get(i).decision == decision) {
                    return decisionPoints.get(i).alternatives[selections.get(i)];
                }
            }
        }

        return super.adaptivePredict(input, decision, outerContext);
    }

    @Override
    public int getUniqueAlt(Collection<ATNConfig> configs) {
        int result = super.getUniqueAlt(configs);

        int t = _input.LA(1);
        if (t == CaretToken.CARET_TOKEN_TYPE) {
            caretToken = (CaretToken)_input.LT(1);
            throw noViableAlt(_input, _outerContext, (ATNConfigSet)configs, _startIndex);
        }

        return result;
    }

    @Override
    protected void closure(ATNConfigSet sourceConfigs, ATNConfigSet configs, boolean collectPredicates, boolean greedy, boolean loopsSimulateTailRecursion, boolean hasMoreContext, PredictionContextCache contextCache) {
        super.closure(sourceConfigs, configs, collectPredicates, greedy, loopsSimulateTailRecursion, hasMoreContext, contextCache);
    }

    protected abstract IntervalSet getWordlikeTokenTypes();

    @Override
    public ATNState getReachableTarget(ATNConfig source, Transition trans, int ttype) {
        if (ttype == CaretToken.CARET_TOKEN_TYPE) {
            ATNState target = null;
            if (trans instanceof AtomTransition) {
                AtomTransition at = (AtomTransition)trans;
                if (getWordlikeTokenTypes().contains(at.label)) {
                    target = at.target;
                }
            } else if (trans instanceof SetTransition) {
                SetTransition st = (SetTransition)trans;
                boolean not = trans instanceof NotSetTransition;
                // TODO: this could probably be done with an intersects method?
                for (int t : getWordlikeTokenTypes().toArray()) {
                    if (!not && st.set.contains(t) || not && !st.set.contains(t)) {
                        target = st.target;
                        break;
                    }
                }
            } else if (trans instanceof RangeTransition) {
                RangeTransition rt = (RangeTransition)trans;
                // TODO: there must be a better algorithm here :)
                int[] wordlikeTokenTypes = getWordlikeTokenTypes().toArray();
                int lb = Arrays.binarySearch(wordlikeTokenTypes, rt.from);
                int ub = Arrays.binarySearch(wordlikeTokenTypes, rt.to);
                if (lb >= 0 || ub >= 0 || lb != ub) {
                    target = rt.target;
                }
            } else if (trans instanceof WildcardTransition) {
                target = trans.target;
            }

            if (caretTransitions == null) {
                caretTransitions = new LinkedHashMap<ATNConfig, List<Transition>>();
            }

            List<Transition> configTransitions = caretTransitions.get(source);
            if (configTransitions == null) {
                configTransitions = new ArrayList<Transition>();
                caretTransitions.put(source, configTransitions);
            }

            configTransitions.add(trans);
            return target;
        }

        return super.getReachableTarget(source, trans, ttype);
    }
}
