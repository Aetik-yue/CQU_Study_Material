-- Copyright 1986-2018 Xilinx, Inc. All Rights Reserved.
-- --------------------------------------------------------------------------------
-- Tool Version: Vivado v.2018.3 (win64) Build 2405991 Thu Dec  6 23:38:27 MST 2018
-- Date        : Mon Oct 13 22:42:29 2025
-- Host        : anbera running 64-bit major release  (build 9200)
-- Command     : write_vhdl -force -mode synth_stub -rename_top mux4x1_block_andgate_10_0 -prefix
--               mux4x1_block_andgate_10_0_ mux4x1_block_andgate_5_0_stub.vhdl
-- Design      : mux4x1_block_andgate_5_0
-- Purpose     : Stub declaration of top-level module interface
-- Device      : xc7a35tcpg236-1
-- --------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

entity mux4x1_block_andgate_10_0 is
  Port ( 
    a : in STD_LOGIC_VECTOR ( 0 to 0 );
    b : in STD_LOGIC_VECTOR ( 0 to 0 );
    c : out STD_LOGIC_VECTOR ( 0 to 0 )
  );

end mux4x1_block_andgate_10_0;

architecture stub of mux4x1_block_andgate_10_0 is
attribute syn_black_box : boolean;
attribute black_box_pad_pin : string;
attribute syn_black_box of stub : architecture is true;
attribute black_box_pad_pin of stub : architecture is "a[0:0],b[0:0],c[0:0]";
attribute X_CORE_INFO : string;
attribute X_CORE_INFO of stub : architecture is "andgate,Vivado 2018.3";
begin
end;
