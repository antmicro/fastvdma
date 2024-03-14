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

package DMAController.CSR
import DMAController.DMADriver
import DMAUtils.DMAModule
import chisel3._
import DMAController.DMAConfig.DMAConfig

class ClearCSR(dmaConfig: DMAConfig) extends DMAModule(dmaConfig) {
  val io = IO(new Bundle {
    val csr = Flipped(new CSRRegBundle(dmaConfig.controlDataWidth))
    val value = Output(UInt(dmaConfig.controlDataWidth.W))
    val clear = Input(UInt(dmaConfig.controlDataWidth.W))
  })

  val reg = RegInit(0.U(dmaConfig.controlDataWidth.W))

  io.csr.dataIn := reg
  io.value := reg

  when(io.csr.dataWrite) {
    reg := io.csr.dataOut
  }.otherwise {
    reg := reg & (~io.clear).asUInt
  }
}

object ClearCSR {
  def apply(clear: UInt, csrCtl: CSRRegBundle, dmaConfig: DMAConfig): UInt = {
    val csr = Module(new ClearCSR(dmaConfig))

    csr.io.clear := clear

    csr.io.csr <> csrCtl

    csr.io.value
  }
}
