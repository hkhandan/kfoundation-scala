package net.kfoundation

import java.io.PrintWriter

import scala.collection.immutable.ListSet

object ObjectSerializer {
  // --- NESTED TYPES --- //

  case class State()
  val ROOT       : State = State()
  val MEMBER     : State = State()
  val OBJECT     : State = State()
  val OBJECT_BODY: State = State()
  val PROPERTY   : State = State()
  val VALUE      : State = State()
  val COLLECTION : State = State()

  case class ValueType()
  val NUMBER: ValueType = ValueType()
  val CHAR  : ValueType = ValueType()
  val STRING: ValueType = ValueType()
  val BOOL  : ValueType = ValueType()
  val NON   : ValueType = ValueType()

  class StackItem(
    val _state: State,
    val _isLead: Boolean,
    val _name: UString)
  {
    def this() = this(ROOT, false, UString.EMPTY)
  }


  // --- STATIC FIELDS --- //

  private val SPACE: UChar = ' '
}

abstract class ObjectSerializer {
import ObjectSerializer._

  // --- FIELDS --- //

  var _state: State = _
  var _isLead: Boolean = _
  var _indentUnit: Int = _
  var _indent: Int = _
  var _index: Int = _
  var _name: UString = _
  var _writer: PrintWriter = _
  var _stack: ListSet[StackItem] = _


  // --- CONSTRUCTORS --- //

//  def this(stream: OutputStream, indentUnit: Int) = this()
//  def this(stream: OutputStream) = this(stream, 4)


  // --- ABSTRACT METHODS --- //

//  protected def printHeader(): Unit
//
//  protected def printAttribute(name: UString,
//    value: Any, valueType: ValueType, isLead: Boolean)

//  protected: virtual void printObjectBegin(RefConst<UString> className,
//    RefConst<UString> name, bool isLead) = 0;
//
//  protected: virtual void printObjectEnd(RefConst<UString> className,
//    bool isLead) = 0;
//
//  protected: virtual void printNull(RefConst<UString> name, bool isLead) = 0;
//
//  protected: virtual void printCollectionBegin(RefConst<UString> name,
//    bool isLead) = 0;
//
//  protected: virtual void printCollectionEnd(bool isLead) = 0;


  // --- METHODS --- //

//  private: Ref<ObjectSerializer> attribute(RefConst<UString> name,
//    const Streamer& value, value_type_t type);
//
//  private: RefConst<UString> stackToString() const;
//  private: RefConst<UString> stateToString(state_t state) const;
//
//  protected: void printIndent(bool newLine = true);
//  protected: PrintWriter& getWriter();
//
//  public: Ref<ObjectSerializer> member(RefConst<UString> name);
//  public: Ref<ObjectSerializer> object(RefConst<UString> className);
//  public: Ref<ObjectSerializer> object(const SerializingStreamer& obj);
//  public: Ref<ObjectSerializer> attribute(RefConst<UString> name, const wchar_t value);
//  public: Ref<ObjectSerializer> attribute(RefConst<UString> name, const kf_int32_t value);
//  public: Ref<ObjectSerializer> attribute(RefConst<UString> name, const kf_int64_t value);
//  public: Ref<ObjectSerializer> attribute(RefConst<UString> name, const double value);
//  public: Ref<ObjectSerializer> attribute(RefConst<UString> name, const bool value);
//  public: Ref<ObjectSerializer> attribute(RefConst<UString> name, const Streamer& value);
//  public: Ref<ObjectSerializer> attribute(RefConst<UString> name, RefConst<UString> value);
//  public: Ref<ObjectSerializer> attribute(RefConst<UString> name);
//  public: Ref<ObjectSerializer> null();
//  public: Ref<ObjectSerializer> endObject();
//  public: Ref<ObjectSerializer> collection();
//  public: Ref<ObjectSerializer> endCollection();
//
//  public: template<typename T>
//  Ref<ObjectSerializer> object(RefConst<T> ptr);
}