package com.github.houbie.lesscss.builder;

import java.util.Collection;

/**
 * Listener interface for compilation notifications.
 */
public interface CompilationListener {
    void notifySuccessfulCompilation(Collection<CompilationUnit> compilationUnits);
}
