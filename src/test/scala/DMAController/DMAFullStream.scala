/*
Copyright (C) 2019-2024 Antmicro

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

SPDX-License-Identifier: Apache-2.0
*/

package DMAController

import DMAController.Bfm.{AxiLiteMasterBfm, AxiStreamMasterBfm, Axi4MemoryBfm}
import DMAController.Bus.{AXI4Lite, AXIStream, AXI4}
import DMAController.Worker.{InterruptBundle, SyncBundle}
import chisel3.{Bits, Bundle}

class DMAFullStream(dut: DMATop) extends DMAFull(dut) {
  val width = 256
  val height = 256
  val io = dut.io.asInstanceOf[Bundle{
                                val control: AXI4Lite
                                val read: AXIStream
                                val write: AXI4
                                val irq: InterruptBundle
                                val sync: SyncBundle}]

  val control = new AxiLiteMasterBfm(io.control, peek[Bits], poke[Bits], println)
  val reader = new AxiStreamMasterBfm(io.read, width * height, peek[Bits], poke[Bits], println)
  val writer = new Axi4MemoryBfm(io.write, width * height, peek[Bits], poke[Bits], println)
}
