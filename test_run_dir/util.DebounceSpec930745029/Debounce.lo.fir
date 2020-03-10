circuit Debounce :
  module Debounce :
    input clock : Clock
    input reset : UInt<1>
    input io_btnU : UInt<1>
    input io_sw : UInt<8>
    output io_led : UInt<8>
  
    reg _T : UInt<1>, clock with :
      reset => (UInt<1>("h0"), _T) @[Debounce.scala 13:38]
    reg btnSync : UInt<1>, clock with :
      reset => (UInt<1>("h0"), btnSync) @[Debounce.scala 13:30]
    reg _T_1 : UInt<7>, clock with :
      reset => (UInt<1>("h0"), _T_1) @[Debounce.scala 18:22]
    node tick = eq(_T_1, UInt<7>("h63")) @[Debounce.scala 19:20]
    node _T_2 = add(_T_1, UInt<1>("h1")) @[Debounce.scala 20:31]
    node _T_3 = tail(_T_2, 1) @[Debounce.scala 20:31]
    node _T_4 = mux(tick, UInt<1>("h0"), _T_3) @[Debounce.scala 20:15]
    reg btnDeb : UInt<1>, clock with :
      reset => (UInt<1>("h0"), btnDeb) @[Debounce.scala 35:19]
    node _GEN_0 = mux(tick, btnSync, btnDeb) @[Debounce.scala 36:15]
    reg _T_5 : UInt<3>, clock with :
      reset => (UInt<1>("h0"), _T_5) @[Debounce.scala 25:22]
    node _T_6 = bits(_T_5, 1, 0) @[Debounce.scala 27:21]
    node _T_7 = cat(_T_6, btnDeb) @[Cat.scala 29:58]
    node _GEN_1 = mux(tick, _T_7, _T_5) @[Debounce.scala 26:14]
    node _T_8 = bits(_T_5, 2, 2) @[Debounce.scala 29:9]
    node _T_9 = bits(_T_5, 1, 1) @[Debounce.scala 29:18]
    node _T_10 = and(_T_8, _T_9) @[Debounce.scala 29:13]
    node _T_11 = bits(_T_5, 2, 2) @[Debounce.scala 29:29]
    node _T_12 = bits(_T_5, 0, 0) @[Debounce.scala 29:38]
    node _T_13 = and(_T_11, _T_12) @[Debounce.scala 29:33]
    node _T_14 = or(_T_10, _T_13) @[Debounce.scala 29:23]
    node _T_15 = bits(_T_5, 1, 1) @[Debounce.scala 29:49]
    node _T_16 = bits(_T_5, 0, 0) @[Debounce.scala 29:58]
    node _T_17 = and(_T_15, _T_16) @[Debounce.scala 29:53]
    node btnFilter = or(_T_14, _T_17) @[Debounce.scala 29:43]
    reg _T_18 : UInt<1>, clock with :
      reset => (UInt<1>("h0"), _T_18) @[Debounce.scala 15:37]
    node _T_19 = eq(_T_18, UInt<1>("h0")) @[Debounce.scala 15:29]
    node risingEdge = and(btnFilter, _T_19) @[Debounce.scala 15:27]
    reg r1 : UInt<8>, clock with :
      reset => (UInt<1>("h0"), r1) @[Debounce.scala 45:19]
    node _T_20 = add(r1, UInt<1>("h1")) @[Debounce.scala 47:14]
    node _T_21 = tail(_T_20, 1) @[Debounce.scala 47:14]
    node _GEN_2 = mux(risingEdge, _T_21, r1) @[Debounce.scala 46:21]
    io_led <= r1 @[Debounce.scala 50:10]
    _T <= io_btnU @[Debounce.scala 13:38]
    btnSync <= _T @[Debounce.scala 13:30]
    _T_1 <= mux(reset, UInt<7>("h0"), _T_4) @[Debounce.scala 20:9]
    btnDeb <= _GEN_0 @[Debounce.scala 37:12]
    _T_5 <= mux(reset, UInt<3>("h0"), _GEN_1) @[Debounce.scala 27:11]
    _T_18 <= btnFilter @[Debounce.scala 15:37]
    r1 <= mux(reset, UInt<8>("h0"), _GEN_2) @[Debounce.scala 47:8]
