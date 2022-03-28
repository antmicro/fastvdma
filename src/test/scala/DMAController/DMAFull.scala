/*
Copyright (C) 2019-2022 Antmicro

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

SPDX-License-Identifier: Apache-2.0
*/

package DMAController

import scala.reflect.runtime.universe._
import DMAController.Bfm._
import DMAController.Bus._
import DMAController.Worker.{InterruptBundle, SyncBundle}
import chisel3.iotesters._
import chisel3._

abstract class DMAFull(dut: DMATop) extends PeekPokeTester(dut){
  val control: ControlBfm
  val reader: IOBfm
  val writer: IOBfm
}
