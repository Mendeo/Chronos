/*После того как экземпляр этого адаптера будет установлен на ListView
 * при клике на элементе ListView в качестве параметра id будет передана
 * переменная, записанная в Map'е с ключём KeyId.
 * Это может быть полезно, когда нужно при клике по элементу ListView получать не просто номер
 * позиции элемента, но и так же его уникальный id, привязанный например к id в базе данных
 * Этот id мы и передаём в данный адаптер с ключём KeyId.
 */

package ru.mendeo.chronos;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.widget.SimpleAdapter;

public class SimpleAdapterWithCustomId extends SimpleAdapter
{
	private String mKeyId;
	private List<? extends Map<String, ?>> mData;
	
	public SimpleAdapterWithCustomId(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to, String keyId)
	{
		super(context, data, resource, from, to);
		mKeyId = keyId;
		mData = data;
	}
	@Override
	public long getItemId(int position)
	{
		super.getItemId(position);
		Long id = (Long)mData.get(position).get(mKeyId);
		return id.longValue();
	}
}
