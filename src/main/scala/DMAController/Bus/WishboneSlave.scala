/*
Copyright (C) 2019-2025 Antmicro

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

SPDX-License-Identifier: Apache-2.0
*/

package DMAController.Bus

import chisel3._
import chisel3.util._

class WishboneSlave(val addrWidth : Int, val dataWidth : Int) extends BusIf {
  /* data */
  val dat_i = Input(UInt(dataWidth.W))
  val dat_o = Output(UInt(dataWidth.W))
  /* control */
  val cyc_i = Input(Bool())
  val stb_i = Input(Bool())
  val we_i = Input(Bool())
  val adr_i = Input(UInt((addrWidth - log2Ceil(dataWidth / 8)).W))
  val sel_i = Input(UInt((dataWidth / 8).W))
  val ack_o = Output(Bool())
  val stall_o = Output(Bool())
  val err_o = Output(Bool())
}
