package net.sf.xmm.moviemanager.util;

import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;


/**
 * Extends the {@link org.apache.log4j.PatternLayout} class by adding the capability
 * to set different conversion patterns for each logging level.  This class will allow log entries
 * to be formatted differently according to the level of the log entry.  For example, entries at
 * the DEBUG level can contain more verbose information, such as the caller which generated the
 * logging event, while entries at the INFO level can be formatted with only a date and the message.
 * This class is thread safe to ensure the string returned by the {@link #format(LoggingEvent) format}
 * method is the one intended for the calling thread.
 * 
 * @author Oliver C. Hernandez
 * @see org.apache.log4j.PatternLayout
 */
public class PatternLayoutByLevel extends PatternLayout {
  
  /** Default pattern string for logging at levels that do not have a pattern set. */
  protected String defaultPattern = DEFAULT_CONVERSION_PATTERN;
  
  /** Pattern string for logging at the DEBUG level. */
  protected String debugPattern   = DEFAULT_CONVERSION_PATTERN;
  
  /** Pattern string for logging at the INFO level. */
  protected String infoPattern    = DEFAULT_CONVERSION_PATTERN;
  
  /** Pattern string for logging at the WARN level. */
  protected String warnPattern    = DEFAULT_CONVERSION_PATTERN;
  
  /** Pattern string for logging at the ERROR level. */
  protected String errorPattern   = DEFAULT_CONVERSION_PATTERN;
  
  /** Pattern string for logging at the FATAL level. */
  protected String fatalPattern   = DEFAULT_CONVERSION_PATTERN;
  
  /** Indicator for when the pattern is set for the DEBUG level. */
  protected boolean debugPatternSet = false;
  
  /** Indicator for when the pattern is set for the INFO level. */
  protected boolean infoPatternSet  = false;
  
  /** Indicator for when the pattern is set for the WARN level. */
  protected boolean warnPatternSet  = false;
  
  /** Indicator for when the pattern is set for the ERROR level. */
  protected boolean errorPatternSet = false;
  
  /** Indicator for when the pattern is set for the FATAL level. */
  protected boolean fatalPatternSet = false;
  
  /**
   * Constructs a PatternLayoutByLevel using the DEFAULT_LAYOUT_PATTERN of the
   * superclass {@link org.apache.log4j.PatternLayout} for all log levels.
   */
  public PatternLayoutByLevel() {
    super();
  }

  /**
   * Constructs a PatternLayoutByLevel using the supplied conversion pattern
   * as the default pattern for log levels that do not have a pattern set.
   * 
   * @param pattern the default pattern to format log events.
   */
  public PatternLayoutByLevel(String pattern) {
    super(pattern);
    this.defaultPattern = pattern;
  }

  /**
   * Set the <b>ConversionPattern</b> option.  This is the string which controls formatting and
   * consists of a mix of literal content and conversion specifiers.  This will be the
   * pattern for log levels that do not have a pattern set for them.
   *
   * @param conversionPattern pattern string to set to.
   */
  public void setConversionPattern(String conversionPattern) {
    this.defaultPattern = conversionPattern;
    super.setConversionPattern(conversionPattern);
  }

  /**
   * Set the <b>ConversionPattern</b> option for logging at the DEBUG level.
   * 
   * @param pattern pattern string for logging at the DEBUG level.
   */
  public void setDebugPattern(String pattern) {
    this.debugPattern = pattern;
    this.debugPatternSet = true;
  }

  /**
   * Set the <b>ConversionPattern</b> option for logging at the INFO level.
   * 
   * @param pattern pattern string for logging at the INFO level.
   */
  public void setInfoPattern(String pattern) {
    this.infoPattern = pattern;
    this.infoPatternSet = true;
  }

  /**
   * Set the <b>ConversionPattern</b> option for logging at the WARN level.
   * 
   * @param pattern pattern string for logging at the WARN level.
   */
  public void setWarnPattern(String pattern) {
    this.warnPattern = pattern;
    this.warnPatternSet = true;
  }

  /**
   * Set the <b>ConversionPattern</b> option for logging at the ERROR level.
   * 
   * @param pattern pattern string for logging at the ERROR level.
   */
  public void setErrorPattern(String pattern) {
    this.errorPattern = pattern;
    this.errorPatternSet = true;
  }

  /**
   * Set the <b>ConversionPattern</b> option for logging at the FATAL level.
   * 
   * @param pattern pattern string for logging at the FATAL level.
   */
  public void setFatalPattern(String pattern) {
    this.fatalPattern = pattern;
    this.fatalPatternSet = true;
  }

  /**
   * Produces a formatted string according to the conversion pattern set for the level of the logging event passed in.
   * 
   * @param event log event to format an entry for.
   * @return a formatted log entry.
   */
  public synchronized String format(LoggingEvent event) {
    
    // Reset the conversion pattern in case it is not set for any log levels.
    super.setConversionPattern(this.defaultPattern);
    
    /*
     * Set the conversion pattern to format according to the log level of the event.  
     * For each log level, check first if a pattern has been set for it.  This will 
     * save execution time by not needlessly calling the methods to check the level
     * of the event.  If the pattern has been set for the log level, then check if
     * it matches the level of the event.  When both of these conditions are true,
     * the conversion pattern is set accordingly, and a format is returned by the
     * format method of the superclass. 
     */
    
    if (this.debugPatternSet) {
      if (event.getLevel().equals(Level.DEBUG)) {
        super.setConversionPattern(this.debugPattern);
      }
    }

    if (this.infoPatternSet) {
      if (event.getLevel().equals(Level.INFO)) {
        super.setConversionPattern(this.infoPattern);
      }
    }

    if (this.warnPatternSet) {
      if (event.getLevel().equals(Level.WARN)) {
        super.setConversionPattern(this.warnPattern);
      }
    }

    if (this.errorPatternSet) {
      if (event.getLevel().equals(Level.ERROR)) {
        super.setConversionPattern(this.errorPattern);
      }
    }

    if (this.fatalPatternSet) {
      if (event.getLevel().equals(Level.FATAL)) {
        super.setConversionPattern(this.fatalPattern);
      }
    }

    return super.format(event);
  }


}
