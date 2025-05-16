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

package DMAController.CSR

import chisel3._
import DMAUtils.DMAModule
import DMAController.DMADriver
import DMAController.DMAConfig._

class SimpleCSR(config: DMAConfig) extends DMAModule(config) {
  val io = IO(new Bundle {
    val csr = Flipped(new CSRRegBundle(config.controlDataWidth))
    val value = Output(UInt(config.controlDataWidth.W))
  })

  val reg = RegInit(0.U(config.controlDataWidth.W))

  io.csr.dataIn := reg
  io.value := reg

  when(io.csr.dataWrite) {
    reg := io.csr.dataOut
  }

}

object SimpleCSR {
  def apply(csrCtl: CSRRegBundle, config: DMAConfig): UInt = {
    val csr = Module(new SimpleCSR(config))

    csr.io.csr <> csrCtl

    csr.io.value
  }
}
